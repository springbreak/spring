## 1.4 제어의 역전 (IoC)

- **UserDaoTest** 는 테스트 이외에도 **ConnectionMaker** 클래스를 만들어서 **UserDao** 에 삽입하는 기능도 가지고 있다. 이를 **DaoFactory** 라는 팩토리 클래스로 분리
<br/><br/>
- **DaoFactory** 를 이용하면 애플리케이션의 컴포넌트 역할을 하는 오브젝트와 애플리케이션의 구조를 결정하는 오브젝트를 분리할 수 있다. (!)
<br/><br/>
- **팩토리 패턴(Factory Pattern)** 은 구체적인 구현 클래스를 숨긴채, 클라이언트가 팩토리로부터 인터페이스를 받아 사용
<br/>
<p align="center"><img src="http://www.oodesign.com/images/stories/factory%20noob%20implementation.gif"/></p>
<br/>
