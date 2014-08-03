package org.gradle.service;

import org.gradle.domain.User;

public class TestUserService extends UserService {
  
  private String id;
  
  public TestUserService(String id) {
    this.id = id;
  }
  
  protected void upgradeLevel(User user) {
    if (user.getId().equals(this.id)) {
      throw new TestUserServiceException();
    }
    
    super.upgradeLevel(user);
  }

}
