
## 3.4 Context 와 DI

`jdbcContextWithResultset` `jdbcContextWithoutResultset` 함수는 보편적인 DAO 로직을 담고 있으므로 클래스로 빼서 다른 DAO 에서 재활용 할 수 있도록 하자.

```java
// UserDao.java

public class UserDao {
	
	@Autowired
	private JdbcContext jdbcContext;
	
	public void deleteAll() throws SQLException {
		jdbcContext.workWithoutResultSet( (c) -> c.prepareStatement("delete from users") );
	}
	
	public int getCount() throws SQLException {
		return jdbcContext.workWithResultset( 
				(Connection c) -> c.prepareStatement("select count(*) from users"));
	}
```

```java
// JdbcContext.java

public class JdbcContext {
	
	@Autowired
	private DataSource ds;
	
	public void workWithoutResultSet(StatementStrategy st) throws SQLException {

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
	
	public int workWithResultset(StatementStrategy st) throws SQLException{
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
}
```

```xml
<bean id="jdbcContext" class="org.gradle.JdbcContext">
</bean>
``` 

그러나 이 방법은 Interface 를 이용하지 않으므로 엄밀히 말해서 개념적으로 DI 는 아니다. 인터페이스 간 관계가 아니라 구체적인 클래스가 설정에 직접 노출이 된다. 

이게 싫다면, `UserDao` 가 `JdbcContext` 를 가지고 직접 DI 를 해줄 수 있다. 은밀히 DI 를 수행하고, 외부에 전략을 감출 수 있지만, 스프링의 DI 를 이용하지 않으므로 싱글톤레지스트리로 만들 수 없다. 그러나 많아봐야 DAO 당 하나씩 오브젝트가 생성될 것 이다.

- 다른 빈을 의존하고 있다면, DI를 통해 다른 오브젝트를 제공받기 위해 자신도 빈으로 등록되어야 한다. 
