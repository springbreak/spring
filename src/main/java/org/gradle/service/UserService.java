package org.gradle.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.gradle.dao.UserDao;
import org.gradle.domain.Level;
import org.gradle.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class UserService {
  
  public static final int MIN_LOGIN_COUNT_FOR_SILVER = 50;
  public static final int MIN_RECOMMEND_COUNT_FOR_GOLD = 30;

	@Autowired
	private UserDao userDao;
	
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

	public void upgradeLevels() throws Exception {
	  
	  TransactionSynchronizationManager.initSynchronization();
	  Connection c = DataSourceUtils.getConnection(dataSource);
	  c.setAutoCommit(false);
	  
	  try {
	    List<User> users = userDao.getAll();

	    for(User u : users) {
	      if (canUpgradeLevel(u)) {
	        upgradeLevel(u);
	      }
	    }
	    c.commit();
	    
    } catch (Exception e) {
      c.rollback();
      throw e;
      
    } finally {
      DataSourceUtils.releaseConnection(c, dataSource);
      TransactionSynchronizationManager.unbindResource(this.dataSource);
      TransactionSynchronizationManager.clearSynchronization();
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
  }
}
