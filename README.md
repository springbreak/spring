
## 5.2.2 글로벌 트랜잭션, JTA

`UserService` **DI** 와 **DataSource** 인터페이스를 적용해서 데이터베이스가 바뀌어도 문제가 없다. 그러나 트랜잭션에서 연결되는 DB 의 개수가 2개 이상이라면 문제가 된다. 현재 JDBC 의 Connection 을 이용하는 방식은 **로컬 트랜잭션** 방식이어서, 특정 DB에 종속된다. 따라서 각 DB 와 독립적으로 만들어지는 Connection 을 통해서가 아니라, 별도의 트랜잭션을 관리자를 통해 트랜잭션을 관리하는  **글로벌 트랜잭션** 이 필요하다. 그래야만 여러개의 DB 가 참여하는 작업을 하나의 트랜잭션으로 만들 수 있고 JMS 처럼 트랜잭션을 지원하는 다른 서비스들도 하나로 묶을 수 있다.

자바는 글로벌 트랜잭션을 위해 **JTA(Java Transaction API)** 를 제공한다.

<p align="center"> <img src="http://jianmingli.com/wp/wp-content/uploads/2012/06/wlsadmin_jee_arch_jta.jpg" /> </p>

**JTA** 를 이용할 경우에도 애플리케이션은 기존 방법 그대로 JDBC, JMS 등의 API 를 사용하되, 트랜잭션만 **JTA** 것을 사용하면 된다. 그러면 Transaction Manager 가 리소스 매니저와 XA 프토토콜을 이용해 연결된다. 여기서 잠깐, JTA 의 Wiki 설명을 보고가자.

> The Java Transaction API (JTA), one of the Java Enterprise Edition (Java EE) APIs, enables distributed transactions to be done across multiple X/Open XA resources in a Java environment. JTA is a specification developed under the Java Community Process as JSR 907. JTA provides for:
>   
> - demarcation[clarification needed] of transaction boundaries
> - X/Open XA API allowing resources to participate in transactions.

**JTA** 를 이용하면 아래와 같은 코드로 글로벌 트랜잭션을 이용할 수 있다. 

```java
InitialContext ctx = new InitialContext();
UerTranscation tx = (UserTransaction)ctx.lookup(USER_TX_JNDI_NAME);

tx.begin();
Connection c = dataSource.getConnection();

try {
  // business logi
  tx.commit();
} catch (exception e) {
  tx.rollback();
  throw e;
} finally {
  c.close();
}
```

## 5.2.4 트랜잭션 서비스 추상화

**글로벌 트랜잭션**을 이용한다 하더라도, 문제가 있다. JTA 를 사용하지 않고 일반적인 로컬 트랜잭션을 사용하거나 트랜잭션을 전혀 사용하지 않는다면 `UserService`의 코드가 수정되어야 한다. 자신의 로직은 그대로여도, 환경에 따라 코드가 변하는 클래스가 되어버렸다. (!)

스프링은 이런 문제를 해결하기 위해 트랜잭션 서비스 추상화 기술을 제공해 준다. 아래 그림은 `PlatformTransactionMaager` 가 JDBC, JTA, Hibernate 등을 어떻게 추상화 해주는가에 대한 요약이다.

<p align="center"> <img src="http://pds7.egloos.com/pds/200711/21/14/c0036214_4743c60f880e6.gif" /> </p>

`PlatformTransactionManager` 를 통해 JDBC 트랜잭션을 사용하는 코드는 아래와 같다.

```java
public void upgradeLevels() {

  PlatformTransactionManager ptm = 
    new DataSourceTransactionManager(dataSource);

  TransactionStatus ts = 
    ptm.getTransaction(new DefaultTransactionDefinition());

  try {
    List<User> users = userDao.getAll();

    for(User u : users) {
      if (canUpgradeLevel(u)) {
        upgradeLevel(u);
      }
    }

    ptm.commit(ts);

  } catch (RuntimeException e) {
    ptm.rollback(ts);
    throw e;
  } 
}
```

트랜잭션 매니저를 **JTA** 로 바꾸려면, PlatformTransactionManager 의 구현체를 **JTATransactionManager** 로 변경하면 된다.

```java
PlatformTransactionManager ptm = new JTATransactionManager();
```

쉽게 트랜잭션 방법을 바꿀 수 있더래도, 여전히 `UserService` 가 구체적인 클래스 이름을 아는것이 문제다. DI 를 통해 해결할 수 있다. 한가지 주의해야 할 사항은, 스프링의 DI 기능을 이용할때 빈으로 등록하려는 클래스가 **멀티스레드에서도 안전한 싱글톤인지** 생각해 보아야 한다는 것이다. 스프링의  `PlatformTransactionManager` 의 구현체는 싱글톤으로 사용해도 안전한 클래스들이다.

```java
// UserService.java
@Autowired
private PlatformTransactionManager transactionManager;

// setter for interited classes
public void setTransactionManager(PlatformTransactionManager ptm) {
  this.transactionManager = ptm;
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

```

아래는 DI 를 위한 `test-application.xml` 파일이다.

```xml
<bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
  <property name="dataSource" ref="dataSource"></property> 
</bean>

```

마지막으로, `UserService.upgradeLevels()` 메소드를 검증 하기 위한 테스트다.

```java

@Autowired
PlatformTransactionManager transactionManager;

@Test
public void upgradeAllOrNothing() {

  userDao.deleteAll();

  String upgradeStopPositionId = users.get(3).getId();

  UserService testUserService = new TestUserService(upgradeStopPositionId);
  testUserService.setUserDao(this.userDao);
  testUserService.setDataSource(this.dataSource);
  testUserService.setTransactionManager(this.transactionManager);

  for(User u : users) {
    userDao.add(u);
  }

  exception.expect(TestUserServiceException.class);
  testUserService.upgradeLevels();

  checkLevel(users.get(1), false);
}
```




