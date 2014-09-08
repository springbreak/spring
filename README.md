
## 6.2 Dynamic Proxy

프록시 클래스를 만들면 일관된 인터페이스를 제공하면서 기능을 덧붙일 수 있다. 그러나 매번 프록시 클래스를 생성하는건 귀찮은 일이다. 사용하지 않는 메소드도 만들어 주어야 하고, 혹시나 호출될까 별도의 처리를 해주어야 한다. 메소드가 5개면 상관없지만, 100개인데, 프록시 클래스를 5개 만들려면 죽을 맛일테다.

자바의 **Reflection API** 를 사용해서 이런 문제를 해결할 수 있다. 프록시를 이용해 기능을 추가하길 원하는 **타깃** 의 인터페이스와, **InvocationHandler** 인터페이스를 모두 구현한 **다이나믹 프록시** 클래스를 이어줌으로써, 다이나믹 프록시에서 메소드가 호출될 때 마다 해당 메소드를 타깃으로 위임할 수 있다.

아래의 예에서는, `MailSender` 인터페이스의 `send` 함수가 불릴 때 마다 로그를 남기는 기능을 구현하기 위해서 다이나믹 프록시를 이용했다.

```java
// See: https://github.com/springbreak/learn-reflection.git

// LoggingHandler.java
public class LoggingHandler implements InvocationHandler {

  MailSender target;
  
  private Logger logger;
  
  public LoggingHandler(MailSender target) {
    this.target = target;
    
    logger = Logger.getLogger("LoggingHandler");
    
    Appender appender;  
    logger.setLevel(Level.ALL);  
    appender = new ConsoleAppender(new SimpleLayout());  
    logger.addAppender(appender); 
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args)
      throws Throwable {
    
    Object result = method.invoke(target, args);
    
    if (method.getName().equals("send")) {
      logger.info("mail sent and logging using dynamic proxy");
    }

    return result;
  }
}
```

`Proxy.newProxyInstance` 메소드를 이용해서 타깃 인터페이스를 조사해서 다이나믹 프록시 클래스를 만들어 낼 수 있다. 이 다이나믹 프록시 클래스에서, 모든 메소드가 호출되는 시점은 `InvocationHandler` 인터페이스를 구현한 오브젝트의 `invoke` 메소드다.

```java
  @Test
  public void testDynamicProxyMailSender() {
    MailSender logProxy = (MailSender)Proxy.newProxyInstance(
            getClass().getClassLoader(), 
            new Class[] { MailSender.class }, 
            new LoggingHandler(new SmtpMailSender()));
    
    assertThat(logProxy.send(mailContext), is(true));
  }
```

다이나믹 프록시는 리턴값으로 오브젝트가 리턴되므로, 캐스팅에 주의할 필요가 있다. 리턴값을 변경하는 경우 캐스팅이 필요한데, 어느 하나의 새로운 리턴타입을 제공하는 메소드가 추가되면 `InvocationHandler` 를 구현하는 클래스가 변경되야 할 수도 있다. 

리플렉션의 장점은 어떤 인터페이스든지 상관없이 해당 핸들러를 재활용 할 수 있다는 점이다. `Object` 타입으로 타깃을 받는 `Method` 인터페이스를 이용하기 때문이다. 