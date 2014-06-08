
## 3.3 JDBC 전략 패턴 적용하기

변하는 부분과 변하지 않는 부분이 있을때는 **전략 패턴 (Strategy Pattern)** 을 적용하는 편이 낫다. 전략 패턴은 변하지 않는 부분을 Context 로 두고, 변하는 부분을 Strategy 로 두어 필요에 따라 Strategy 를 교체해서 사용하는 방법이다. 

<p align="center"><img src="http://upload.wikimedia.org/wikipedia/commons/3/39/Strategy_Pattern_in_UML.png" /></p>

그리고 Context 가 사용할 전략은 Client 가 필요에 따라 생성해서 주입함으로써 DI 스럽게 구현할 수 있다.

<p align="center"><img src="http://www.cs.tut.fi/~mda/documentation/eclipse/strategy_pattern_diagram.png" /></p>

### Applying Strategy Pattern

#### before : deleteAll

```java

public void deleteAll() throws SQLException {
	Connection c = null;
	PreparedStatement ps = null;

	try {
		c = ds.getConnection();
		ps = c.prepareStatement("delete from users");
		ps.executeUpdate();
		
	} catch (SQLException e) {
		throw e;
		
	} finally {
		if (ps != null) {
			try {
				ps.close();
			} catch (SQLException e) {
				throw e;
			}
		}
		
		if (c != null) {
			try {
				c.close();
			} catch (SQLException e) {
				throw e;
			}
		}
	}
	
	ps.close();
	c.close();
}

```

#### after : deleteAll

변하지 않는 부분은 예외처리 부분이고, 변하는 부분은 `PreparedStatement`, 즉 쿼리를 생성하는 부분이다. 변하지 않는 부분(Context) 를 별개의 메소드로 뽑아낸다. 그리고 기존에 `deleteAll` 메소드는 Client 의 역할을 담당한다.

```java

// Strategy Interface
public interface StatementStrategy {	
	public PreparedStatement createStatement(Connection c) throws SQLException;
}

// Strategy Implement
public class DeleteStrategy implements StatementStrategy {

	@Override
	public PreparedStatement createStatement(Connection c) throws SQLException {
		PreparedStatement ps = c.prepareStatement("delete from users");
		return ps;
	}
}

// Context
public void jdbcContext(StatementStrategy st) throws SQLException {

	Connection c = null;
	PreparedStatement ps = null;

	try {
		c = ds.getConnection();
		ps = st.createStatement(c);
		ps.executeUpdate();
		
	} catch (SQLException e) {
		throw e;
		
	} finally {
		if (ps != null) {
			try {
				ps.close();
			} catch (SQLException e) {
				throw e;
			}
		}
		
		if (c != null) {
			try {
				c.close();
			} catch (SQLException e) {
				throw e;
			}
		}
	}
	
	ps.close();
	c.close();
}

// Client
public void deleteAll() throws SQLException {
		
	StatementStrategy st = new DeleteStrategy();
	jdbcContext(st);
}
```

#### before : add

`UserDao` 의 `getCount()` 메소드는 `ResultSet` 이 있기 때문에 try / catch 구문이 더 복잡했다. 

```java
public int getCount() throws SQLException {
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {

                c = ds.getConnection();
                ps = c.prepareStatement("select count(*) from users");
                rs = ps.executeQuery();

                rs.next();
                int count = rs.getInt(1); 
                return count;
        } catch (Exception e) {
                throw e;

        } finally {
                if (rs != null) {
                        try {
                                rs.close();
                        } catch (Exception e) {
                                throw e;
                        }
                }
                
                if (ps != null) {
                        try {
                                ps.close();
                        } catch (Exception e) {
                                throw e;
                        }
                }
                
                if (c != null) {
                        try {
                                c.close();
                        } catch (Exception e) {
                                throw e;
                        }
                }
        }
}
```

#### after : add

```java

// class UserDao Methods

public void deleteAll() throws SQLException {
        StatementStrategy st = new DeleteStrategy();
        jdbcContextWithoutResultSet(st);
}

public int jdbcContextWithResultset(StatementStrategy st) throws SQLException{
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {

                c = ds.getConnection();
                ps = st.createStatement(c);
                rs = ps.executeQuery();

                rs.next();
                int count = rs.getInt(1); 
                return count;
        } catch (Exception e) {
                throw e;

        } finally {
                if (rs != null) {
                        try {
                                rs.close();
                        } catch (Exception e) {
                                throw e;
                        }
                }
                
                if (ps != null) {
                        try {
                                ps.close();
                        } catch (Exception e) {
                                throw e;
                        }
                }
                
                if (c != null) {
                        try {
                                c.close();
                        } catch (Exception e) {
                                throw e;
                        }
                }
        }
}

public int getCount() throws SQLException {
        
        StatementStrategy st = new GetCountStrategy();
        return jdbcContextWithResultset(st);
        
}

// StatementStrategy Implement for getCount

public class GetCountStrategy implements StatementStrategy {

	@Override
	public PreparedStatement createStatement(Connection c) throws SQLException {
	
		PreparedStatement ps = c.prepareStatement("select count(*) from users");
		return ps;
	}

}
```

### Micro Dependency Injection

DI 는 클래스 간에서 관계가 다이나믹하게 설정되고 주입되야만 DI 가 아니다. 위의 예제에서는 클래스간, 혹은 메소드간 의존관계가 설정되고 주입되지만, 이 또한 DI 라고 볼 수 있다. 이런 경우 작은 단위에서 DI 가 일어난다고 **Micro DI** 라 부르기도 한다.

### Local Class

전략 패턴을 적용해 코드를 ~~아름답게~~ 객체지향스럽게 작성했지만, DAO 의 메소드마다 새로운 Class, File 이 생긴다는 문제점이 있다. 이를 해결하기 위해 StatementStrategy 의 구현 클래스들을 메소드 내의 클래스, **Local Class** 로 바꾸자. 아니. 더 좋은 방법이 있다. `DeleteStategy` 와 같은 StatementStrategy 구현 클래스는 한번밖에 안쓰이므로 이름도 필요가 없다. 익명 클래스로 하자.

```java
public void deleteAll() throws SQLException {
        StatementStrategy st = new StatementStrategy() {
                
                @Override
                public PreparedStatement createStatement(Connection c) throws SQLException {
                        PreparedStatement ps = c.prepareStatement("delete from users");
                        return ps;
                }
        };
        
        jdbcContextWithoutResultSet(st);
}

public int getCount() throws SQLException {
        
        StatementStrategy st = new GetCountStrategy();
        st = new StatementStrategy() {
                
                @Override
                public PreparedStatement createStatement(Connection c) throws SQLException {
                        PreparedStatement ps = c.prepareStatement("select count(*) from users");
                        return ps;
                }
        };
        
        return jdbcContextWithResultset(st);
        
}
```

### Lambda

우린 Java 8 세대다. 로컬 클래스를 람다로 바꿔보자. 

```java
public void deleteAll() throws SQLException {
	jdbcContextWithoutResultSet( (c) -> c.prepareStatement("delete from users") );
}

public int getCount() throws SQLException {
	return jdbcContextWithResultset( 
			(Connection c) -> c.prepareStatement("select count(*) from users"));
}
```


