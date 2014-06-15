
## 3.6 Spring JdbcTemplate

`JdbcContext` 처럼 예외처리에 대한 템플릿을 가지고 있는 Built-in 클래스가 있다. ~~여태까지 무슨짓을 한건가~~ `org.springframework.jdbc.core.JdbcTemplate` 다

```java
// UserDao.java
	@Autowired
	private JdbcTemplate jt;
```

```xml
// test-applicationContext.xml
<bean id="jdbcTemplate" class="org.springframework.jdbc.core.JdbcTemplate">
	<property name="dataSource" ref="dataSource"></property>
</bean>
```

### jdbcTemplate.update

`update` 함수는 기본적으로 `PreparedStatementCreate` 를 받아, `createPreparedStatement(Connection c)` 를 호출한다. 

```java
public void deleteAll() throws SQLException {
	jt.update(new PreparedStatementCreator() {
		
		@Override
		public PreparedStatement createPreparedStatement(Connection con)
				throws SQLException {
			// TODO Auto-generated method stub
			return con.prepareStatement("delete from users");
		}
	});
}
```

그러나 `PreparedStatement` 에 별다른 작업을 해주지 않을 경우 오버라이딩 되어 쿼리 스트링만 인자로 받는 함수가 오버로딩되어 있다.

```java
public void deleteAll() throws SQLException {
	jt.update("delete from users");
}
```

Update 쿼리에 추가적인 인자가 필요할 경우 다음과 같이 사용할 수 있다. `Usedao.add()` 

```java
public void add(User user) throws SQLException {
	jt.update("INSERT INTO users(id, name, password) VALUES (?, ?, ?);",
			user.getId(), user.getName(), user.getPassword());
}
```

### jdbcTemplate.queryForInt()

`ResultSet` 이 필요한 경우 `JdbcTemplate.query()` 를 이용할 수 있다. `getCount()` 처럼 Int 타입이 필요한 경우 특별히 `JdbcTemplate.queryForInt()` 를 사용하면 된다. 먼저 `query()` 를 이용한 샘플을 보면 첫번째 인자로  `PreparedStatement` 를 돌려주는 콜백 오브젝트를, 두번째 인자로 `ResultSet` 를 조작하는 콜백 오브젝트를 파라미터로 넘겨준다.

```java
public int getCount() throws SQLException {

	return jt.query(new PreparedStatementCreator() {
		@Override
		public PreparedStatement createPreparedStatement(Connection con)
				throws SQLException {
			return con.prepareStatement("select count(*) from users");
		}
	}, new ResultSetExtractor<Integer>() {
		@Override
		public Integer extractData(ResultSet rs) throws SQLException,
				DataAccessException {
			rs.next();
			return rs.getInt(1);
		}
	});
}
```

`queryForInt()` 를 사용하게 되면, 훨씬 심플해진다.

```java
public int getCount() throws SQLException {
	return jt.queryForInt("select count(*) from users");
}
```

### jdbcTemplate.queryForObject()

그런데 사실 `JdbcTemplate.queryForObject` 는 Deprecated 되었다.

```java
public int getCount() throws SQLException {
	return jt.queryForObject("select count(*) from users", Integer.class);
}
```

`UserDao.get` 의 경우에도 `queryForObject` 를 이용해서 오브젝트를 가져올 수 있다.

```java
public User get(String id) throws SQLException {

	return jt.queryForObject(
			"SELECT * from users WHERE id = ?",
			new Object[] {id},
			new RowMapper<User>() {
				@Override
				public User mapRow(ResultSet rs, int rowNum)
						throws SQLException {

					User user = new User();
					user.setId(rs.getString("id"));
					user.setName(rs.getString("name"));
					user.setPassword(rs.getString("password"));
					return user;
				}});
}

// lambda version
public User get(String id) throws SQLException {

	return jt.queryForObject(
			"SELECT * from users WHERE id = ?",
			new Object[] {id},
			(rs, rowNum) -> {
				User user = new User();
				user.setId(rs.getString("id"));
				user.setName(rs.getString("name"));
				user.setPassword(rs.getString("password"));
				return user;
			});
}
```

`ResultSetExtractor` 은 한번만 호출되는 콜백 오브젝트인 반면 `RowMapper` 는 여러번 호출 될 수 있다. 

### jdbcTemplate.query

`query()` 는 기본적으로 `List<T>` 를 리턴한다. 따라서 `RowMapper` 가 N번 호출될 수 있다. 아래 `getAll()` 메소드에서 `RowMapper` 는 `Row` 수 만큼 호출되어 매번 `User` 를 리턴하고, 그것이 모여 `query` 가 끝나는 시점에  `List<User` 가 된다.

```java
public List<User> getAll() {
	return jt.query(
			"SELECT * FROM users",
			(rs, rowNum) -> {
				return new User(rs.getString(1), rs.getString(2), rs.getString(3));
			});
}
```





















