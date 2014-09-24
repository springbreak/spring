package lambda.service;

import lambda.domain.User;
import lambda.service.UserServiceImpl;

public class TestUserService extends UserServiceImpl {
  
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
