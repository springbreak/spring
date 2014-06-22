## 4 JdbcTemplate 의 예외처리 

이전에 `JdbcContext` 를 직접 만들어 썼을때에 비해, 스프링의 `JdbcTemplate` 을 사용하면 `add` 와 같은 DAO 메소드의 프로토타입에 `throws SQLException` 을 제거할 수 있다. 무슨 일이 일어난건가!?!

스프링의 `JdbcTemplate` 가 **체크 예외** 를 먹고, **언체크 예외** 를 뱉어낸 것이다. 이 두 가지가 무엇인지 알아보자.

<br/>
<p align="center"><img src="http://voyager.deanza.edu/~hso/cis35a/lecture/java13/images/Picture1.png" /></p>
(출처 - http://voyager.deanza.edu/~hso/cis35a/lecture/java13/intro/hier.html)
<br/>

### Checked Exception, Unchecked Exception

예외(Exception)는 두 종류로 나뉘어진다. 

1. **체크 예외** 는 프로그래머가 `try catch` 로 무조건 처리해야 한다. 그렇지 않으면 IDE 에서 빨간 줄을 그어버린다. `SQLException` 과 같은 체크 예외 클래스들은 `Exception` 클래스를 상속받아 만들어진다. 
2. **언체크 예외** 또는 **런타임 예외** 들은 선택적으로 처리할 수 있다. `NullPointerException` 과 같은 런타임 예외들은 모두 `RuntimeException` 을 상속받는다 

그리고 예외가 아닌 **에러(Error)** 들은, 시스템적으로 문제가 생겼을때 발생한다. 이것들은 잡아봐야 프로그래머가 어찌할 수 없다. `OutOfMemoryError` 를 어쩔건가! 따라서 시스템 레벨에서 특별한 작업을 하는게 아니라면 애플리케이션 수준에서 에러는 무시하자.

그렇다면, 스프링의 `JdbcTemplate` 는 왜 체크 예외를 런타임 예외로 바꾼걸까? 이걸 이해하려면, 예외처리 방법에 대해 논할 필요가 있다. 

### Exception 처리 방법
   
#### 1. 예외 복구

예외를 잡아, 정상적인 로직으로 돌려놓는 방법이다. 파일이 없으면, 다른파일을 읽도록 유도한다거나

#### 2. 예외 회피

예외 처리를 자신이 담당하지 않는 경우 밖으로 던진다

#### 3. 예외 전환

예외를 복구할 수 없거나 다른 이유로 예외를 밖으로 던지지만, 예외를 바꿔버린다. `SQLException` 을 받아 `DuplicatedUserIdException` 을 던질 수 있다. 밖에서 잡아봐야 처리할 수 없는 예외일 경우, 아니면 밖에서 받아봐야 의미 없는 예외일 경우 런타임예외로 바꿀 수 있다.

반대로 체크 예외를 던질 수도 있다. 애플리케이션 비즈니스 로직상으로 처리가 필요한 예외는, 이를테면 잔고가 부족하다던지, 예외를 잡는 쪽에서 반드시 처리를 헤야한다. 이럴때는 의도적으로 체크 예외를 던질 수 있다. 그러나 일반적으로 무조건 체크 예외를 사용하는 것은 무의미 하다. 보편적으로 런타임 예외를 사용해 메소드의 프로토타입에서  `throws` 제거하고, 필요한 경우만 예외를 잡아 처리하고, 반드시 처리해야 한다면 체크예외로 던지는 편이 낫다. ~~고 한다~~

  
### JDBC 의 한계


SQLException 은 대부분의 경우 애플리케이션단에서 복구 불가능 하다. 네트워크, DB 서버, 커넥션 풀, 문법 오류, 제약조건 위반을 어떻게 코드에서 해결할 것인가?  ID 중복 정도라면 모를까. 따라서 `JdbcTemplte` 에선 기게적인 `throws` 선언이 등장하는것을 막기위해 runtime 예외로 변환한다.

예외를 다른 것으로 바꿔 던지는 예외 전환의 목적은 언급 했듯이,

1. 런타임 예외로 포장해서 굳이 필요하지 않은 catch/throws 구문 제거
2. 로우레벨 예외를 좀 더 의미있고 추상화된 예외로 변환

스프링의 `JdbcTemplate` 가 던지는 `DataAccessExveption` 은 런타임 예외다. 그러나 `JdbcTemplate` 는 단순히 `SQLException` 을 잡아 런타임 예외로 바꾸는 것보다 더 많은 일을 한다. 이것을 이야기 하기 전에, JDBC 에 대해 좀 더 알아보자.

### JDBC

JDBC 는 자바를 이용해 DB에 접근하는 방법을 추상화되 API 형태로 정의해놓고, 각 DB 업체가 JDBC 표준을 따라 만들어진 드라이버를 제공하게 해준다 따라서 자바 개발자들은 `Resultset` 등의 일관된, 표준 인터페이스를 통해 기능을 사용하여 개발할 수 있다. 그러나 JDBC 는 다음과 같은 문제점이 있다.

#### 1. 비표준 SQL

웹서비스에서 사용되는 페이징 쿼리 같은 비표준 SQL 을 사용할 수 있다. 이 경우 DAO 를 DB 별로 만들거나, SQL 구문을 코드에서 분리해야 한다.  

#### 2. SQLException 의 DB 에러 정보는 호환성이 없다.  

DB 마다 에러의 종류와 원인도 다 다르다. 그래서, JDBC 는 그냥 SQL Exception 하나로 모두 담아 버린다. 막상 사용할려고 `getErrorCode()` 를 호출하여도 얻어진 에러 코드는 DB 마다 너무나 다르다. `MysqlErrorNumbers.EP_DUP_ENTRY` 처럼

그래서 JDBC는 예외가 발생했을때의 SQL 상태정보를 `SQLException` 에 제공한다. `getSQLState()` 메소드를 이용해 상태정보를 가져올 수 있고, 이 상태정보는 DB 별로 다른 에러코드를 대신할 수 있도록 표준 스펙을 따르도록 되어있다. 그러나 문제는 JDBC 에서 상태정보를 정확하게 만들어주지 않아 이 상태정보를 믿을 수가 없다는 것이다. 비록 JDBC 가 4.0 부터 SQLException 이 아니라 DB 에러에 따라 서브클래싱 한 예외를 던져주더라도, 이 예외가 상태정보를 기반으로 만들어지므로 사용하기 좀 그렇다. 결국 JDBC 에서 제공하는, 호환성 없는 에러코드와 표준을 잘 따르지 않는 상태코드를 가진 SQLException 만으로는 유연한 코드를 작성할 수 없다.

### Spring DataAccessException

이런 불편함을 스프링의 `JdbcTemplate`가  해결해준다. `DataAccessExeption` 은 단순히 `SQLException` 을 런타임 예외로 바꾼게 아니다. 사용하는 데이터베이스에 적합한 DB  에러코드를 이용해 만들어진 예외 객체다. 예를들어, 키가 중복되었을 경우 `DataAccessExeption` 을 상속받은 `DuplicatedKeyException` 을 던진다. 


하나 더 생각해볼 거리가 있다. 만약 다른 종류의 퍼시스턴트 계층을 사용할 때 마다 예외가 다르다면, 아래와 같이 코드를 작성해야 하고, 인터페이스를 활용할 수 없다.

<br/>
```java 
public void add(User u) SQLException; // jdbc
public void add(User u) HibernateException; // Hibernate
```

Spring 은 이것도 해결해 준다. `DataAccessException` 은 JDBC 에서만 쓰이는 것이 아니다. JPA, Hibernate 같은 퍼시스턴트 계층을 위해서도 쓰일 수 있도록 설계된 보편적인 예외다. 


### Summary

- 예외는 복구하거나, 전달하거나, 적절한 예외로 전환되야 한다.
- 애플리케이션의 로직을 담기위한 예외는 체크예외로 만든다
- 복구할 수 없는 예외는 가능한 빨리 런타임 예외로 전환하자
- SQLException 의 에러코드는 DB 종속적이므로, DB 독립적인 예외를 이용하자. 
스프링은 DataAccessException 을 제공한다.
- DAO 를 데이터엑세스 기술에서 독립시키려면 인터페이스 도입, 런타임 예외 전환, 
기술 독립적인 추상화된 예외로의 전환이 이 3가지가 필요하다.
