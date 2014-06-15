
## 3.5 Template Callback Pattern

### 템플릿 (Template)

템플릿은 미리 만들어 놓고, 재활용 하는 것으로 변하지 않는 부분을 지칭한다.

### 콜백 (Callback)

본래 콜백은 실행되길 기대하는 함수다. 다른 언어에서는 함수가 일급-객체(First-Class Object) 이기 때문에 함수 그 자체를 넘겨줄 수 있지만, 자바는 람다가 지원되지 않으므로 호출되길 기대하는 함수를 포함한 오브젝트를 넘겨준다. 그래서 **콜백-오브젝트** 라 부른다. 자바8 에 람다가 추가되긴 했지만. 사실 익명 내부 클래스를 간소화한 것에 불과하다. 람다처럼 보이긴 하나, 실제로는 콜백-오브젝트다.

### 템플릿 콜백 패턴

<p align="center"><img src="http://cfile29.uf.tistory.com/image/122C37544D3535FB1FE5CF" /></p>
<br/>

템플릿 콜백 패턴은 전략 패턴에서 교체될 수 있는 전략을 익명내부 클래스 혹은 람다의 형태로 전달하는 것을 말한다. 그리고 이렇게 넘겨진 전략은 콜백이라 불린다.  

### 람다를 이용한 콜백 패턴

```java
	public void add(User user) throws SQLException {
		jdbcContext.workWithoutResultSet( (c) -> {
			String query = "INSERT INTO users(id, name, password) VALUES (?, ?, ?);";
			PreparedStatement ps = c.prepareStatement(query);
			ps.setString(1, user.getId());
			ps.setString(2, user.getName());
			ps.setString(3, user.getPassword());
			
			return ps;
		});
	}
	
	public void deleteAll() throws SQLException {
		jdbcContext.workWithoutResultSet( (c) -> c.prepareStatement("delete from users") );
	}
	
	public int getCount() throws SQLException {
		return jdbcContext.workWithResultset( 
				(Connection c) -> c.prepareStatement("select count(*) from users"));
	}
```
