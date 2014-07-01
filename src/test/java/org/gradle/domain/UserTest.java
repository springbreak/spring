package org.gradle.domain;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class UserTest {
  
  User user;

  @Before
  public void setUp() {
    user = new User();
  }
  
  @Test
  public void upgradeLevel() {
    Level[] levels = Level.values();
    
    for(Level lv : levels) {
      if (lv.nextLevel() == null) continue;
      
      user.setLevel(lv);
      user.upgradeLevel();
      
      assertThat(user.getLevel(), is(lv.nextLevel()));
    }
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void cannotUpgradeLevel() {
    Level[] levels = Level.values();
    
    for(Level lv : levels) {
      if (lv.nextLevel() != null) continue;
      
      user.setLevel(lv);
      user.upgradeLevel();
    }
  }
  
  
}
