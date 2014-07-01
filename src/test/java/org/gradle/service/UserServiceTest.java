package org.gradle.service;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import static org.gradle.service.UserService.MIN_LOGIN_COUNT_FOR_SILVER;
import static org.gradle.service.UserService.MIN_RECOMMEND_COUNT_FOR_GOLD;

import java.util.Arrays;
import java.util.List;

import org.gradle.dao.UserDao;
import org.gradle.domain.Level;
import org.gradle.domain.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/test-applicationContext.xml")
public class UserServiceTest {

  @Autowired
  UserService userService;
 
  @Autowired
  UserDao userDao;

  List<User> users;

  @Before
  public void setUp() {
    users = Arrays.asList( 
        new User("basic1", "name1", "p1", Level.BASIC, MIN_LOGIN_COUNT_FOR_SILVER-1, 0),
        new User("basic2", "name2", "p2", Level.BASIC, MIN_LOGIN_COUNT_FOR_SILVER, 0),
        new User("silver1", "name3", "p3", Level.SILVER, 60, MIN_RECOMMEND_COUNT_FOR_GOLD-1),
        new User("silver2", "name4", "p4", Level.SILVER, 59, MIN_RECOMMEND_COUNT_FOR_GOLD),
        new User("silver3", "name5", "p5", Level.SILVER, 60, MIN_RECOMMEND_COUNT_FOR_GOLD),
        new User("gold", "name6", "p6", Level.GOLD, 100, 100));
  }  

  @Test
  public void add() {
    userDao.deleteAll();
    
    User userWithLevel = users.get(4);
    User userWithoutLevel = users.get(0);
    userWithoutLevel.setLevel(null);
    
    userService.add(userWithLevel);
    userService.add(userWithoutLevel);
    
    assertThat(userWithLevel.getLevel(), is(userWithLevel.getLevel()));
    assertThat(userWithoutLevel.getLevel(), is(Level.BASIC));
  }

  @Test
  public void upgradeLevels() {
    userDao.deleteAll();
    
    for(User u : users) {
      userDao.add(u);
    }
    
    userService.upgradeLevels();
    
    checkLevel(users.get(0), false);
    checkLevel(users.get(1), true);
    checkLevel(users.get(2), false);
    checkLevel(users.get(3), true);
    checkLevel(users.get(4), true);
    checkLevel(users.get(5), false);
    
  }

  private void checkLevel(User user, boolean upgraded) {
    User userUpgraded = userDao.get(user.getId());
    
    if (upgraded) {
      assertThat(userUpgraded.getLevel(), is(user.getLevel().nextLevel()));
    } else {
      assertThat(userUpgraded.getLevel(), is(user.getLevel()));
    }
    
  }
}
