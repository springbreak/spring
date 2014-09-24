
## 6.3 다이나믹 프록시를 이용한 트랜잭션 부가기능

앞에선 트랜잭션을 위해서 직접 `UserTranscationServie` 라는 프록시 클래스를 만들었었지만, 리플렉션을 이용해 다이나믹 프록시를 생성할 수 있는 방법을 배웠으므로 적용해 보자.

```java
// TxHandler.java

public class TxHandler implements InvocationHandler {
	
	private Object target;
	private String pattern;
	private PlatformTransactionManager txManager;

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public void setTxManager(PlatformTransactionManager txManager) {
		this.txManager = txManager;
	}

	public void setTarget(Object target) {
		this.target = target;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		
		if (method.getName().startsWith(pattern)) {
			return invokeInTranscation(method, args);
		} else {
			return method.invoke(target, args);
		}
	}

	private Object invokeInTranscation(Method method, Object[] args) throws Throwable {
		
		TransactionStatus txStats = 
				txManager.getTransaction(new DefaultTransactionDefinition());
		
		try {
			Object ret = method.invoke(target, args);
			txManager.commit(txStats);
			return ret;
		} catch (InvocationTargetException e) {
			txManager.rollback(txStats);
			throw e.getTargetException();
		}
	}
}
```

테스트 코드는 다음처럼 작성한다.

```java
// UserServiceTest.java
...
...

	@Test
	public void upgradeAllOrNothing() {

		userDao.deleteAll();

		String upgradeStopPositionId = users.get(3).getId();

		UserServiceImpl testUserService = new TestUserService(upgradeStopPositionId);
		testUserService.setUserDao(this.userDao);
		testUserService.setDataSource(this.dataSource);
		testUserService.setMailSender(mailSender);

		TxHandler txHandler = new TxHandler();
		txHandler.setTarget(testUserService);
		txHandler.setTxManager(this.transactionManager);
		txHandler.setPattern("upgradeLevels");

		// Create a proxy class dynamically using txHandler
		UserService txUserService = (UserService) Proxy.newProxyInstance(
				getClass().getClassLoader(), new Class[] { UserService.class },
				txHandler);

		for (User u : users) {
			userDao.add(u);
		}

		exception.expect(TestUserServiceException.class);
		txUserService.upgradeLevels();

		checkLevel(users.get(1), false);
	}

	private void checkLevel(User user, boolean upgraded) {
		User userUpgraded = userDao.get(user.getId());

		if (upgraded) {
			assertThat(userUpgraded.getLevel(), is(user.getLevel().nextLevel()));
		} else {
			assertThat(userUpgraded.getLevel(), is(user.getLevel()));
		}
	}

...
...
```

### 팩토리 빈

제대로 동작 하지만, 문제가 한 가지 있다. 다이나믹 프록시로 만든 클래스는 일반적인 클래스가 아니기 때문에, 스프링의 빈으로 등록할 방법이 없다.

왜냐하면 스프링은 지정된 클래스 이름을 이용해 `Class.forName("beanName).newInstance()` 와 같은 방법으로 인스턴스를 생성하는데, 다이나믹 프록시를 이용하면 `Proxy.newProxyInstance` 라는 스태틱 메소드를 통해서 `InvocationHandler` 의 구현체를 받아 인스턴스를 만들기 때문에 클래스 이름을 입력하지 않는다.

스프링은 이런 문제점을 해결하기 위해서, 디폴트 생성자를 이용해 빈을 만드는 방법 뿐 아니라 **팩토리 빈** 을 이용해 빈을 생성할 수 있는 방법을 제공한다.

```java
public class Message {
	private String text;

	private Message(String text) {
		this.text = text;
	}
	
	public String getMessage() {
		return this.text;
	}
	
	static public Message getMessage(String text) {
		return new Message(text);
	}
}
```

다음과 같은 클래스가 있을 때, 스프링의 빈으로 `Message` 클래스를 등록하면, 인스턴스를 만들어 주긴 한다. 리플렉션은 `private` 를 무시할 수 있기 때문이다. 그러나 생성자를  `private` 로 만들고 스태틱 팩토리 메소드를 제공하는 데는 다 이유가 있기 때문에, 스프링이 이 방법으로 인스턴스를 만들도록 **팩토리 빈** 을 만들고 등록해보자.

```java
// MessageFactoryBean.java

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/FactoryBeanTest-context.xml")
public class FactoryBeanTest {

	@Autowired
	ApplicationContext context;
	
	@Test
	public void getMessageFromFactoryBean() {
		
		Object msg = context.getBean("message");
		assertThat(msg, instanceOf(Message.class));
		assertThat(((Message)msg).getMessage(), is("Example"));
	}
}
``` 

```xml
<!-- MessageFactoryBean-context.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
	
	<bean id="message" class="learning.test.MessageFactoryBean">
		<property name="text" value="Example"></property>
	</bean>
</beans>
```

```java
// MessageFactoryBean.java

public class MessageFactoryBean implements FactoryBean<Message>{
	
	String text;

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public Message getObject() throws Exception {
		// TODO Auto-generated method stub
		return Message.getMessage(text);
	}

	@Override
	public Class<?> getObjectType() {
		// TODO Auto-generated method stub
		return Message.class;
	}

	@Override
	public boolean isSingleton() {
		// TODO Auto-generated method stub
		return false;
	}
}
```

만약 팩토리 빈 그 자체를 가지고 오고 싶으면, 빈 이름에 `&` 를 붙이면 된다.

```java
	@Test
	public void getMessageFactoryBean() {
		Object factoryBean = context.getBean("&message");
		assertThat(factoryBean, instanceOf(MessageFactoryBean.class));
	}
```

이제 `TxHandler` 를 이용해 다이나믹 프록시를 생성하는 팩토리 빈,  `TxProxyFactoryBean`을 만들어 보자. `TxHandler` 는 재활용 할 수 있는 다이나믹 프록시 인스턴스를 만들 수 있기 때문에, `implements FactoryBean<Object>` 를 선언했다. 즉, 만들어지는 다이나믹 프록시는 `Object` 타입이다. 다양한 타입의 다이나믹 프록시를 생성해야하므로, `Proxy.newProxyInstance` 의 세번째 인자로 들어갈 클래스 오브젝트를 입력 받기 위해 `serviceInterface` 라는 멤버 변수를 선언했다.  

```java
// TxProxyFactoryBean.java

public class TxProxyFactoryBean implements FactoryBean<Object>{
	
	private Object target;
	private String pattern;
	private PlatformTransactionManager txManager;
	private Class<?> serviceInterface;

	public void setTarget(Object target) {
		this.target = target;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public void setTxManager(PlatformTransactionManager txManager) {
		this.txManager = txManager;
	}

	public void setServiceInterface(Class<?> serviceInterface) {
		this.serviceInterface = serviceInterface;
	}

	@Override
	public Object getObject() throws Exception {
		
		TxHandler txHandler = new TxHandler();
		txHandler.setPattern(pattern);
		txHandler.setTarget(target);
		txHandler.setTxManager(txManager);
		
		return Proxy.newProxyInstance(
				getClass().getClassLoader(),
				new Class[] { serviceInterface },
				txHandler);
	}

	@Override
	public Class<?> getObjectType() {
		return serviceInterface;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}
}
```

이렇게 정의된 클래스를 다음과 같이 스프링 빈으로 등록한다.

```xml
<bean id="userService" class="lambda.service.TxProxyFactoryBean">
  <property name="target" ref="userServiceImpl"></property>
  <property name="txManager" ref="transactionManager"></property>
  <property name="pattern" value="upgradeLevels"></property>
  <property name="serviceInterface" value="lambda.service.UserService"></property>
</bean>
```

아래는 테스트코드다. `userService` 빈은 `target` 프로퍼티를 세팅해야 하는데, 테스트를 위해서는 `testUserService` 클래스가 필요하다. 따라서 XML 에서 등록된 `userServiceImpl` 을 이용하지 않으므로 컨텍스트를 변경하게 되어 `@DirtyContext` 어노테이션을 사용했다.

당연히, `context.getBean("userService")` 로 가져온 빈에 대해서는 테스트가 실패한다. 왜냐하면 `target` 이 `userServiceImpl` 이기 때문이다.

```java
// in UserServiceTest.java

	@Test
	@DirtiesContext
	public void upgradeLevels() throws Exception {
		userDao.deleteAll();

		for (User u : users) {
			userDao.add(u);
		}

		MockMailSender mockMailSender = new MockMailSender();
		userService.setMailSender(mockMailSender);
		userService.upgradeLevels();

		checkLevel(users.get(0), false);
		checkLevel(users.get(1), true);
		checkLevel(users.get(2), false);
		checkLevel(users.get(3), true);
		checkLevel(users.get(4), true);
		checkLevel(users.get(5), false);

		List<String> request = mockMailSender.getRequests();
		assertThat(request.size(), is(3));
		assertThat(request.get(0), is(users.get(1).getEmail()));
		assertThat(request.get(1), is(users.get(3).getEmail()));
		assertThat(request.get(2), is(users.get(4).getEmail()));
	}
```

테스트를 위해서 따로 테스트용 설정, 즉 테스트용 빈을 만들어 주거나, `TxProxyFactoryBean` 코드를 확장하는 방법이 있을수 있겠지만 여기서는 빈으로 등록된 `TxProxyFactoryBean` 을 `&` 을 이용해서 직접 가져 온 후 컨텍스트를 변경하는 방법을 사용했다.

### Proxy Factory Bean : Pros and Cons

무엇보다도, 프록시 팩토리 빈의 장점은 **Target** 에 상관없이 재활용이 가능하다는 점이다. 예를 들어 `coreService` 인터페이스의 구현체인 `coreServiceImpl` 의 모든 메소드에 트랜잭션 기능이 필요하다면,

```xml
<bean id="coreService" class="service.CoreServiceImpl">
  <property name="dao" ref="dao" />
</bean>
``` 

이 설정 코드를 다음처럼 바꿔주기만 하면 된다. 참고로, `pattern` 을 빈 문자열로 만들면 모든 메소드가 `String.startWith()` 필터링에 걸리기 때문에 모든 메소드에 트랜잭션이 적용된다.

```xml
<bean id="coreServiceTarget" class="service.CoreServiceImpl">
  <property name="dao" ref="dao" />
</bean>

<bean id="coreService" class="service.TxProxyFactoryBean">
  <property name="target" ref="**coreServiceTarget**" />
  <property name="txManager" ref="txManager" />
  <property name="pattern" value="dao" />
  <property name="serviceInterface" ref="**service.CoreService**" />
  <property name="dao" ref="dao" />
</bean>
``` 

**데코레이터 패턴 방식의 프록시**는, 모든 메소드를 구현해야 하므로 번거롭고, 다수의 메소드에 코드 중복이 발생할 수 있었지만 **프록시 팩토리 빈** 은 이 두 가지 문제를 해결해 준다. 그럼에도 불구하고, **프록시 팩토리 빈** 은 두 가지 문제를 가지고 있다.

> (1). 한번에 다수의 클래스에 부가기능을 적용할 수 없다. 각 클래스마다 부가기능 갯수 만큼의 설정파일의 코드가 반복될 것이다. 만약 200개의 클래스가 있고, 3개의 부가기능을 모두 적용하려면 설정파일의 코드가 6줄이라 가정할 때, 18 * 200개 만큼의 코드가 반복이 되야한다. 하나의 타깃에 여러개의 부가기능을 적용할때도 같은 문제가 발생한다. 타깃과 인터페이스만 다른 코드가 중복된다. 코드 추가 없이 부가기능을 설정파일 변경만으로 추가할 수 있는건 놀랍지만, 여전히 문제가 있다.

<br/>

> (2). `TxHandler` 가 타깃 갯수만큼 생성되는 것도 문제다. 이는 `TxHandler` 가 타깃을 프로퍼티로 가지고 있기 때문이다. 싱글톤 빈으로 만들어 타깃에 상관없이 재활용 할 수 없을까?