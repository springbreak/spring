package org.gradle.service;

import java.util.List;

import javax.sql.DataSource;

import org.gradle.dao.UserDao;
import org.gradle.domain.Level;
import org.gradle.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class UserService {
  
  public static final int MIN_LOGIN_COUNT_FOR_SILVER = 50;
  public static final int MIN_RECOMMEND_COUNT_FOR_GOLD = 30;
  
  @Autowired
  private MailSender mailSender;
  
  public void setMailSender(MailSender ms) {
    this.mailSender = ms;
  }

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private PlatformTransactionManager transactionManager;
	
	public void setTransactionManager(PlatformTransactionManager ptm) {
	  this.transactionManager = ptm;
	}
	
	public void setUserDao(UserDao userDao) {
	  this.userDao = userDao;
	}

	@Autowired
	private DataSource dataSource;
	
	public void setDataSource(DataSource ds) {
	  this.dataSource = ds;
	}
	
	public void add(User u) {
	  if (u.getLevel() == null) { 
	    u.setLevel(Level.BASIC);
	  }
	  
	  userDao.add(u);
	}

	public void upgradeLevels() {
	  
	  TransactionStatus ts = 
	      this.transactionManager.getTransaction(new DefaultTransactionDefinition());
	  
	  try {
	    List<User> users = userDao.getAll();

	    for(User u : users) {
	      if (canUpgradeLevel(u)) {
	        upgradeLevel(u);
	      }
	    }
	    
	    this.transactionManager.commit(ts);
	    
    } catch (RuntimeException e) {
      this.transactionManager.rollback(ts);
      throw e;
    } 
	}

  public boolean canUpgradeLevel(User u) {
    
    Level currentLevel = u.getLevel();
    
    switch(currentLevel) {
      case BASIC: return (u.getLogin() >= MIN_LOGIN_COUNT_FOR_SILVER);
      case SILVER: return (u.getRecommend() >= MIN_RECOMMEND_COUNT_FOR_GOLD);
      case GOLD: return false;
      default: throw new IllegalArgumentException("Unknowl Level :" +
          currentLevel);
    }
  }

  protected void upgradeLevel(User u) {
    u.upgradeLevel();
    userDao.update(u);
    sendUpgradeEmail(u);
  }
  
  private void sendUpgradeEmail(User u) {
	 // send email
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(u.getEmail());
    message.setFrom("admin@service.com");
    message.setSubject("Level Upgraded");
    message.setText("your level is upgraded to " + u.getLevel());
    
    mailSender.send(message);
  }
}
