## 1.5 스프링의 IoC

- 기본적으로 **build.gradle** 에 의존성으로  **compile 'org.springframework:spring-context:4.0.5.RELEASE'** 를 삽입하면 필요한 의존성을 자동으로 검색해 추가해 준다.
  1. spring-context
  2. spring-beans
  3. spring-aop
  4. spring-core
  5. spring-expression
<br/><br/>  
- **@Configuration** 은 이 클래스가 애플리케이션 컨텍스트 또는 빈 팩토리가 사용할 설정 정보임을 표시한다.
- **@Bean** 은 오브젝트 생성을 담당하는 IoC 용 메소드라는 표시
<br/>
```java
@Configuration
public class DaoFactory {

	@Bean
	public UserDao userDao() {
		// TODO Auto-generated method stub
		ConnectionMaker cm = getConnectionMaker();
		UserDao dao = new UserDao(cm);
		return dao;
	}

	@Bean
	public ConnectionMaker getConnectionMaker() {
		return new GConnectionMaker();
	}

}
```
<br/>
- 이렇게 등록된 설정 정보는 ApplicationContext 에서 사용되고, getBeans 메소드를 통해 오브젝트를 생성한다.
<br/>
```java
ApplicationContext context =
		new AnnotationConfigApplicationContext(DaoFactory.class);
		
UserDao dao = context.getBean("userDao", UserDao.class);
```
<br/><br/>
- 애플리케이션 컨텍스트를 이용하면 구체적인 팩토리 클래스를 알 필요가 없어서 좋다.
<br/><br/>

