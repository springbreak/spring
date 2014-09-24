package lambda.util;

import org.springframework.mail.*;


public class DummyMailSender implements MailSender{

  @Override
  public void send(SimpleMailMessage simpleMessage) throws MailException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void send(SimpleMailMessage[] simpleMessages) throws MailException {
    // TODO Auto-generated method stub
    
  }
}
