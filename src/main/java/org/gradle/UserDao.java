package org.gradle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

public class UserDao {
	
	@Autowired
	private DataSource ds;
	
	public void setDataSource(DataSource ds) {
		this.ds = ds;
	}
	
	public void deleteAll() throws SQLException {
		Connection c = ds.getConnection();
		
		PreparedStatement ps = c.prepareStatement("delete from users");
		ps.executeUpdate();
		ps.close();
		c.close();
	}
	
	public int getCount() throws SQLException {
		Connection c = ds.getConnection();
		PreparedStatement ps = c.prepareStatement("select count(*) from users");
		ResultSet rs = ps.executeQuery();
		
		rs.next();
		int count = rs.getInt(1); 
		
		rs.close();
		ps.close();
		c.close();
		
		return count;
	}
	
	public void add(User user) throws SQLException {
		// TODO Auto-generated method stub
		
		Connection c = ds.getConnection();
		
		String query = "INSERT INTO users(id, name, password) VALUES (?, ?, ?);";
		PreparedStatement ps = c.prepareStatement(query);
		ps.setString(1, user.getId());
		ps.setString(2, user.getName());
		ps.setString(3, user.getPassword());
		
		ps.executeUpdate();
		ps.close();
		c.close();
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
