
## 4 예외와 JdbcTemplate 의 예외처리 

- 예외는 잡기만 해선 안된다. 반드시 처리해야 하고 통보되어야 한다.

자바에서 throw 를 통해 발생시킬 수 있는 예외는 크게 3가지가 있다.


- 애플리케이션 예외 : 로직상 발생하는 예외, 체크예외로 만들어서 처리하도록 ex) 잔고부족

SQLException 은 99% 경우 애플리케이션단에서 복구 불가. 네트워크, DB 서버, 커넥션 풀, 문법 오류, 제약조건 위반을 어떻게 코드에서 해결? ID 중복 정도라면 모를까/ 따라서 jdbcTemplte 에선 기게적인 throws 선언이 등장하는것을 막기위해 runtime 예외로 전환.

- 예외를 다른 것으로 바꿔 던지는 예외 전환의 목적은

1. 런타임예외로 포장해서 굳이 필요하지 않은 catch/throws 제거
2. 로우레벨 예외 좀 더 의미있고 추상화된 예외로 바꿔

스프링의 jdbcTemplate 가 던지는 DataAccessExveption 은 일단 런타임 예외. 


JDBC 는 자바를 이용해 DB에 접근하는 방법을 추상화되 API 형태로 정의해놓고, 각 DB 업체가 JDBC 표준을 따라 만들어진 드라이버를 제공하게 해줌. 따라서 자바 개발자들은 Resultset 등의 표준 인터페이스를 통해 기능을 사용. 일관된 기능을 이용해 개발 가능


JDBC 의 한계

1. 비표준 SQL

-> DAO를 DB별로 만들거나 SQL 분리
웹프로그램 페이징 쿼리?

2. 호환성없는 SQL Exception 의 DB 에러정보

DB마다 에러의 종류와 원인도 제각각 이므로 JDBC 는 그냥 SQL Exception 하나로 모두 담아버림. 
근데 getErrorCode() 로 가져올 수 있는 DBpfjzhemrk DB 별로 모두 다름. 벤더마다 다른 에러코드이기 때문.

MysqlErrorNumbers.EP_DUP_ENTRY 가 그 예

그래서 SQLException 은 예외가 발생했을때 DB상태를 담은 SQL 상태 정보를 부가적으로 제공. 
getSQLState 메소드로 상태정보를 가져올 수 있는데, 이건 DB 별로 다른 에러코드를 대신할 수 있도록
SQL 상태코드 스펙을 따르도록 되어있음.

근데 문제는 JDBC 드라이버에서 상태정보를 정확하게 만들어주지 않음. 
그래서 이 상태코드를 믿을 수 없음.

호환성 없는 에러코드와 표준을 잘 따르지 않는 상태코드를 가진 SQLException 만으로는 유연한 코드 작성 불가

스프링은 SQLException 을 그냥 DataAccessException 으로 Runtime 화 하는게 아니라
DB 에러코드를 이용해 적절히 서브클래싱해서 의미있는 예외 객체를 던짐.

JDBC 도 4.0 도 되면서 SQLException 을 서브클래싱 한걸 던져주므로 
좀 나아지긴 했지만 여전히 상태정보를 이용한다는점에서 좀 그럼.

Spring 의 DataAccessException 은 DB 종류별 JDBC 만이 아니라
JPA, 다른 ORM 에 대한 것도 제공하므로 조음.

예를들어 

public void add(User u) SQLException; // jdbc
public void add(User u) HibernateException; // Hibernate

Sprin

- 예외는 복구하거나, 전달하거나, 적절한 예외로 전환되야 한다.
- 애플리케이션의 로직을 담기위한 예외는 체크예외로 만든다
- 복구할 수 없는 예외는 가능한 빨리 런타임 예외로 전환하자
- SQLException 의 에러코드는 DB 종속적이므로, DB 독립적인 예외를 이용하자. 
스프링은 DataAccessException 을 제공한다.

DAO 를 데이터엑세스 기술에서 독립시키려면 인터페이스 도입과 런타임예외전환
기술에 독립적인 추상화된 예외로 전환이 필요하다.
