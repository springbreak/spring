
## 2.3 JUnit을 이용한 단위 테스트
- **UserDaoTest** 테스트를 매번 직접 실행해야 하고, 테스트 결과를 눈으로 직접 확인해야 하므로 번거롭다.
<br/><br/>
- 테스트는 가능하다면 하나의 메소드만 검증하는것이 좋다.
- 예외가 올바르게 던져지는 것도 JUnit 을 이용하면 검증할 수 있다. **@Test(expected=Exception.class)** 을 이용하자.
- 네거티브 테스트부터 시작하자. ID 를 조회하는 DAO 메소드를 만든다면, ID 가 올바르게 던져지지 않는 테스트부터 작성할 것

### TDD (Test Driven Development)

- 실패한 테스트를 성공시키기 위한 목적이 아닌 코드를 만들지 않는 것이 원칙
- TDD 를 하면 자연스럽게 단위 테스트를 만들 수 있다.
- 빠르게 오류를 발견해 이른시기에 대처가 가능하다
- 엔터프라이즈 환경에서 애플리케이션의 테스트를 만들기가 어려**웠**으나 스프링은 엔터프라이즈 애플리케이션의 테스트를 빠르고 쉽게 작성할 수 있는 매우 편리한 기능을 많이 제공한다.

### Fixture

테스트를 수행하는데 필요한 정보나 오브젝트를 **픽스쳐(fixture)** 라고 한다. 픽스쳐는 여러 테스트에서 반복적으로 사용되므로 **@Before** 메소드를 이용해 한번에 초기화 하는편이 낫다.

```java

private UserDao dao; 

@Before
public void setUp() {
	ApplicationContext ct = 
		new GenericXmlApplicationContext("applicationContext.xml");
		
		this.dao = ct.getBean("userDao", UserDao.class);
}
```