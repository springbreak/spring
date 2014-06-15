package org.gradle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

public class UserDao {
	
	@Autowired
	private JdbcContext jdbcContext;
	
	@Autowired
	private DataSource ds;
	
	public void deleteAll() throws SQLException {
		jdbcContext.workWithoutResultSet( (c) -> c.prepareStatement("delete from users") );
	}
	
	public int getCount() throws SQLException {
		return jdbcContext.workWithResultset( 
				(Connection c) -> c.prepareStatement("select count(*) from users"));
	}

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

	public User get(String id) throws SQLException {
		// TODO Auto-generated method stub
		Connection c = ds.getConnection();

		String query = "SELECT * from users WHERE id = ?";

		PreparedStatement ps = c.prepareStatement(query);
		ps.setString(1, id);

		User user = null;

		ResultSet rs = ps.executeQuery();
		if ( rs.next() ) {
			user = new User();
			user.setId(rs.getString("id"));
			user.setName(rs.getString("name"));
			user.setPassword(rs.getString("password"));
		}

		if (user == null) {
			throw new EmptyResultDataAccessException(1);
		}

		rs.close();
		ps.close();
		c.close();

		return user;
	}
}
