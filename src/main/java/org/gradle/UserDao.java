package org.gradle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {
	
	private ConnectionMaker connectionMaker;
	
	public void setConnectionMaker(ConnectionMaker cm) {
		this.connectionMaker = cm;
	}
	
	public void add(User user) throws ClassNotFoundException, SQLException {
		// TODO Auto-generated method stub
		
		Connection c = connectionMaker.getConnection();
		
		String query = "INSERT INTO users(id, name, password) VALUES (?, ?, ?);";
		PreparedStatement ps = c.prepareStatement(query);
		ps.setString(1, user.getId());
		ps.setString(2, user.getName());
		ps.setString(3, user.getPassword());
		
		ps.executeUpdate();
		ps.close();
		c.close();
	}

	public User get(String id) throws ClassNotFoundException, SQLException {
		// TODO Auto-generated method stub
		Connection c = connectionMaker.getConnection();
		
		String query = "SELECT * from users WHERE id = ?";
		
		PreparedStatement ps = c.prepareStatement(query);
		ps.setString(1, id);
		
		ResultSet rs = ps.executeQuery();
		rs.next();
		User user = new User();
		user.setId(rs.getString("id"));
		user.setName(rs.getString("name"));
		user.setPassword(rs.getString("password"));
		
		rs.close();
		ps.close();
		c.close();
		
		
		return user;
	}
}
