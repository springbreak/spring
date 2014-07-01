package org.gradle.service;

import java.util.List;

import org.gradle.dao.UserDao;
import org.gradle.domain.Level;
import org.gradle.domain.User;
import org.springframework.beans.factory.annotation.Autowired;

public class UserService {
  
  public static final int MIN_LOGIN_COUNT_FOR_SILVER = 50;
  public static final int MIN_RECOMMEND_COUNT_FOR_GOLD = 30;

	@Autowired
	public UserDao userDao;
	
	public void add(User u) {
	  if (u.getLevel() == null) { 
	    u.setLevel(Level.BASIC);
	  }
	  
	  userDao.add(u);
	}

	public void upgradeLevels() {
	  List<User> users = userDao.getAll();
	  
	  for(User u : users) {
	    if (canUpgradeLevel(u)) {
	      upgradeLevel(u);
	    }
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

  private void upgradeLevel(User u) {
    u.upgradeLevel();
    userDao.update(u);
  }

}
