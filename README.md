
## 2.4 Spring 을 이용한 테스트

### ApplicationContext

매번 테스트 오브젝트를 만들때 마다 `ApplicationContext` 를 만드는건 부담이다. 지금은 빈이 몇개 없지만, 몇백, 몇천개 되면 느려진다. 따라서 `@Test` 가 실행될 때 마다 새롭게 생성되는 모든 테스트 오브젝트가 ApplicationContext 를 공유하도록 해 보자.

```java
// add dependency 'org.springframework:spring-test:4.0.5.RELEASE'

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/applicationContext.xml")
public class UserDaoTest {
	
	@Autowired
	private ApplicationContext context;
	private UserDao dao;
	
	@Before
	public void setUp() {

		this.dao = context.getBean("userDao", UserDao.class);
		
		System.out.println(this.context);
		System.out.println(this.dao);
	}
	
	...

// Result
org.springframework.context.support.GenericApplicationContext@d70c109 
org.gradle.UserDao@2df32bf7
org.springframework.context.support.GenericApplicationContext@d70c109 
org.gradle.UserDao@2df32bf7	

```

### Autowired

`@Autoired` 은 컨텍스트 내에서 해당 변수와 일치하는 타입의 빈을 찾아 자동으로 DI 를 해준다 ~~오오~~ 그런데 앞에서 우리는 ApplicationContext 를 빈으로 등록하지 않았지만 `@Autoired` 가 먹혔다. 왜 그럴까? 스프링은 기본적으로 애플리케이션 컨텍스트 그 자신도 빈으로 등록한다. 사실 `@Autowired` 가 있으면 ApplicationContext 를 찾을 필요도 없다.
 
### Free-Container TEST

컨테이너나 프레임워크가 있어야 DI 를 할 수 있는것은 아니다. 편해질 뿐. 우리가 직접 테스트 코드를 위한 관계 설정을 해 줄 수 있다.

```java

@Before
public void setUp() {
	dao = new UserDao();
	DataSource dataSource = new SingleConnectionDataSource(
		"jdbc:mysql://localhost/springtest", "springdev", "test", true);
		
		dao.setDataSource(dataSource);
}

```

스프링은 침투적(Invasive) 기술이기 때문에 애플리케이션 코드에 API 가 등장한다. 스프링 없이 테스트할 수 있는 방법을 우선적으로 고려하자. 이 방법이 수행속도가 빠르고 코드가 간결하다.
