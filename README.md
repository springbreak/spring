
## 5.2.1 트랜잭션 

사용자 레벨 업그레이드 작업을 하던 중 예외가 발생하면, 작업중 변경된 사용자의 레벨을 다시 초기화 하고 싶을 때는 트랜잭션을 사용해야 한다. 트랜잭션 구현에 앞서서, 먼저 트랜잭션을 어떻게 테스트 할 것인가? 네트워크를 끊어버리거나, DB 연결을 끊는 작업으로는 테스트를 자동화 할 수 없다. 그렇다고 실제 서비스 코드인 `UserService` 에서 예외를 던질 수도 없다. 테스트를 위해서 실제 코드를 수정해서는 안된다.

차라리, `UserService` 코드의 일부 메소드를 Protected 를 바꾸어 상속받을 수 있게 한 뒤, 이것을 이용해 테스트 클래스를 만들자. 

```java
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
```

```java
public class TestUserServiceException extends RuntimeException {

}
```

그러면 아래의 코드를 통해 테스트 할 수 있다.

```java
@Test
public void upgradeAllOrNothing() {

  userDao.deleteAll();

  String upgradeStopPositionId = users.get(3).getId();
  UserService testUserService = new TestUserService(upgradeStopPositionId);
  testUserService.setUserDao(this.userDao);

  for(User u : users) {
    userDao.add(u);
  }

  try {

    testUserService.upgradeLevels();
    fail("TestUserServiceException Expected");
  } catch (TestUserServiceException e) {
    // TODO: handle exception
  }

  checkLevel(users.get(1), false);
}
```

기대했던 결과와는 다르게, `checkLevel` 함수에서 테스트가 실패하는걸 확인할 수 있다. 이는 우리가 트랜잭션을 사용하지 않았기 때문이다. 트랜잭션은 더 이상 쪼갤 수 없는 작업의 단위를 말한다. `UserService` 의 `upgradeLevels` 메소드도 분리할 수 없는 하나의 단위가 되어야, 성공시 모두 업그레이드 되고, 실패시 모두 롤백 될 수 있다. 

DB 는 그 자체로 완벽한 트랜잭션을 지원한다. SQL 을 이용해 업데이트 할 경우 하나의 **Column** 만 업데이트 되는 경우는 없다. **Row** 가 업데이트 되거나, 아니거나 둘 중 하나다. 하지만 다수의 SQL 문장을 실행하는 경우에는 이 여러개의 SQL 구문을 묶어 트랜잭션으로 취급해 주어야 한다.


#### JDBC 트랜잭션

JDBC 에서 트랜잭션으로 코드를 묶으려면, 해당 로직의 앞과 뒤에 `Connection.setAutoCommit(false)` 와 `Connection.commit()` / `Connection.rollback()` 를 선언해 주어야 한다. 자동으로 커밋되는것을 막고, 예외 발생시 롤백을 시도하거나, 로직이 성공적으로 수행되었을 때만 커밋을 직접 한다. 

```java
Connection c = dataSource.getConnection();

c.setAutoCommit(false); 

try {
  // SQL 1
  // SQL 2
  // SQL 3

  c.commit();
} catch (Exception e) {
  c.rollback();
} 

c.close();
```

그런데, 현재 트랜잭션으로 묶일 코드는 `UserService.upgradeLevels` 메소드 내부에 있고, 여기서 커넥션을 직접 생성하면 `UserDao` 의 메소드 호출 시 파라미터로 넘겨주어야 한다. 이 커넥션을 써야 트랜잭션으로 쿼리가 묶이기 때문이다. 이렇게 커넥션을 직접 넘겨주면 JDBC Template 를 활용할 수도 없다. 그렇다고, `upgradeLevels` 메소드를 `UserDao` 로 넘기는 것도 비합리적이다. 비즈니스 로직과 모델의 코드가  뒤섞이기 때문이다.

그리고 `Connection` 을 `UserDao` 의 인터페이스에 추가하는 순간, `UserDao` 는 데이터 엑세스 기술에 독립적이지 않게 된다. JPA 혹은 Hibernate 로 `UserDao` 의 구현을 변경하려고 하면, `EntityManager` 나 `Sessions` 오브젝트를 전달 받아야 하므로 `UserDao` 의 인터페이스를 수정해야 한다.

만약 이 `Connection` 을 `UserService` 의 멤버 변수로 저장해 공유한다고 해도, 멀티스레드 환경에서는 변수의 값이  덮어 씌워지는 문제가 발생할 수 있다. 

#### Spring 의 트랜잭션 동기화

스프링 제공하는 독립적인 트랜잭션 동기화를 사용하면 이 문제를 해결할 수 있다. 비즈니스 로직인 `UserService` 에서 커넥션을 생성하고 저장소에 보관해 둔다. 그리고 `JDBC Template` 는 동기화 저장소에 생성되어있는 커넥션이 있을 경우에만, 그 커넥션을 이용한다. 

```java
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
```

스프링이 제공하는 `DataSourceUtils` 를 통해 저장소에 커넥션을 만들수 있고, 동기화 하기 위해 `TransactionSynchronizationManager` 를 이용하면 된다. 아래는 테스트 코드 특정 예외가 검출되는지 확인하기 위해  JUnit 4.7 부터 추가된 `@Rule ExpectedException` 을 이용했다.

```java
@Test
public void upgradeAllOrNothing() throws Exception {

  userDao.deleteAll();

  String upgradeStopPositionId = users.get(3).getId();

  UserService testUserService = new TestUserService(upgradeStopPositionId);
  testUserService.setUserDao(this.userDao);
  testUserService.setDataSource(this.dataSource);

  for(User u : users) {
    userDao.add(u);
  }

  exception.expect(TestUserServiceException.class);
  testUserService.upgradeLevels();

  checkLevel(users.get(1), false);
}

```



