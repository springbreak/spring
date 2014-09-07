
## 5.4 메일 서비스 추상화

사용자의 레벨이 업그레이드 될 때 마다 사용자에게 메일을 보내는건 어렵지 않다. 문제는 테스트다. 

- 메일 서버가 없을 경우에는 어떻게 테스트 할 것인가?
- 매번 테스트마다 메일을 보내야 하는가? 테스트가 거대하다면 리소스 문제가 생기진 않을까?
- 메일 서버는 애플리케이션 외부에 있다. 어떻게 메일이 보내졌음을 테스트할 것인가?

**JavaMail API** 는 검증된 모듈이므로 직접 구동하는 것 대신에, 요청이 들어가는것만 확인하면 된다. 실제 운영 데이터베이스 대신에 테스트 DB 를 사용한 것처럼 테스트 메일 서버 인터페이스를 만들어 보려고 헀으나...

```java
Session s = Session.getInstance(props, null);
```

자바 메일에서는 **Session** 오브젝트를 만들어야만 메일을 전송할 수소 있는데, 이놈은 상속이 불가능한 **Final Class** 인 데다가, 생성자가 **private** 여서 스태틱 오브젝트를 생성하려면 팩토리 메소드를 이용해서 만드는 방법밖에 없다.

스프링은 이런 문제를 해결하기 위해서 **MailSender** 인터페이스를 제공한다. `'org.springframework:spring-context-support:4.1.0.RELEASE'` 를 라이브러리에 추가함으로서 **org.springframework.mail.*;** 를 임포트 할 수 있다. **MailSender** 인터페이스를 이용해새 자바 메일을 사용하려면, **JavaMailServiceImpl** 을 생성하면 된다. 반대로, 테스트 메일 서버를 만들고 싶으면 이 인터페이스를 구현한 클래스를 만들면 된다. 

<br/>
<p align="center">
<img src="http://www.javatpoint.com/sppages/images/springmailapi.jpg" />
</p>
<br/>

```java
public class UserService {
  
  @Autowired
  private MailSender mailSender;
  
  public void setMailSender(MailSender ms) {
    this.mailSender = ms;
  }
  
  ...
  ...
  
  protected void upgradeLevel(User u) {
    u.upgradeLevel();
    userDao.update(u);
    sendUpgradeEmail(u);
  }
  
  private void sendUpgradeEmail(User u) {
	 // send email
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(u.getEmail());
    message.setFrom("admin@service.com");
    message.setSubject("Level Upgraded");
    message.setText("your level is upgraded to " + u.getLevel());
    
	mailSender.send(message);
  }
```
<br/>

자바 메일을 추상화 한 스프링의 메일 패키지를 이용해서, 테스트 메일 서버를 구현할 수 있다. **MailMessage** 를 구현한 **SimpleMailMessage** 에 보낼 메일을 채워 넣고, 실제 메일을 보낼 경우에는  **MailSender** 를 구현한 **JavaMailSenderImpl** 을, 아니라면 해당 인터페이스를 구현한 **DummyMailSender** 를 이용하면 된다.

그러나 이 메일 전송 서비스는 트랜잭션 개념이 적용되지 않았다. 만약 구현한다면, 업그레이드가 끝난 후 메일을 모두 보내거나, 트랜잭션을 구현한 **MailSender** 를 만들 수 있겠다. 전자가 파라미터로 메일 저장용 객체를 들고 다녀야하고, 메일 발송을 위한 트랜잭션이 비즈니스 로직(UserService)과 섞이는 단점이 있다면, 후자는 서로 다른 종류의 작업을 분리해 처리할 수 있다. 
  

###테스트 대역(Test Double)

`DummyMailSender` 처럼 테스트 대상의 의존 오브젝트로서 테스트 동안에 코드가 정상적으로 돌아갈 수 있도록 도와주는 것이 **테스트 스텁(Test Stub)** 이다. 테스트 스텁은 테스트 더블의 대표적인 예다. 테스트 스텁은 때때로 결과를 주거나, 예외를 던질 수도 있다.

이외에도 파라미터를 검증하거나, 테스트하길 원하는 함수를 몇 번 호출했는지를 검증하고 싶을때도 있다. 이럴때는 메소드의 리턴값을 검증하는 것만으로는 불가능하며, **목 오브젝트(Mock Object)** 를 이용하면 된다. 목오브젝트는 테스트 오브젝트와 자신의 사이에서 일어나는 커뮤니케이션 내용을 저장해 두었다가 테스트 결과를 검증하는데 활용할 수 있다.

작성한 코드로 다시 돌아가 보면, 테스트 `upgradeAllOrNothing` 의 경우 트랜잭션을 테스트하기 위한 것이므로 아무 행위도 하지 않는 `DummyMailSender` 면 충분하지만, 업그레이드 기능을 확인하는 `upgradeLevels` 테스트의 경우에는 메일 발송도 테스트해야 한다. 자바 메일을 직접 이용했다면, 실제 메일이 왔는지 확인하거나 로그를 뒤져서 메일 발송 로그가 남아있는지를 확인해야 하지만, 스프링의 메일 서비스 추상화를 이용하면 목 오브젝트를 이용해서 발송 여부를 확인할 수 있다.

```java

public class MockMailSender implements MailSender {
  
  private List<String> requests = new ArrayList<String>();
  
  public List<String> getRequests() {
    return this.requests;
  }

  @Override
  public void send(SimpleMailMessage simpleMessage) throws MailException {
    // TODO Auto-generated method stub
    // Save first email address only
    requests.add(simpleMessage.getTo()[0]);
  }
}
```

해당 테스트는 컨텍스트가 변경되므로 `@DirtiesContext` 를 지정해주고, 코드는 아래와 같이 작성하면 된다.

```java
// UserSerivceTest.java

  @Test
  @DirtiesContext
  public void upgradeLevels() throws Exception {
    userDao.deleteAll();

    for(User u : users) {
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