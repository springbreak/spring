## 1.3

- 커넥션과 관련된 기능을 별개의 클래스인 **SimpleConnectionMaker** 로 분리  
<br/>
- 그러나 클래스 이름을 구체적으로 아는것은 변경에 취약하므로 인터페이스로 구현  
<br/> 
- 인터페이스를 도입했음에도 여전히 **new GConnectionMaker()** 구문 때문에 구체적인 클래스를 알아야함. 따라서 **UserDaoTest** 에서 생성자에 **ConnectionMaker** 클래스를 주입한다.  
<br/>
- 디자인 패턴 관점에서, 이 구조는 **전략 패턴(Strategy Pattern)** 이며 객체지향 설계 원칙 중 **개방 폐쇄 원칙** 과 **높은 응집도와 낮은 결합도** 로도 설명 가능하다.  
<br/>
<p align="center"><img src="http://upload.wikimedia.org/wikipedia/commons/3/39/Strategy_Pattern_in_UML.png" style=" display:block; margin: 0px auto;"/></p>
<br/>
- **전략 패턴** 은 클라이언트, 컨텍스트, 전략으로 구성되어 있다. 클라이언트는 전략을 생성해서 클라이언트에 삽입하며 필요에 따라 전략을 교체할 수 있다.   
<br/>
