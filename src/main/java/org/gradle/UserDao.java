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
	private DataSource ds;
	
	public void setDataSource(DataSource ds) {
		this.ds = ds;
	}
	
	public void jdbcContextWithoutResultSet(StatementStrategy st) throws SQLException {

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
	
	public void deleteAll() throws SQLException {
		jdbcContextWithoutResultSet( (c) -> c.prepareStatement("delete from users") );
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
		return jdbcContextWithResultset( 
				(Connection c) -> c.prepareStatement("select count(*) from users"));
	}

	public void add(User user) throws SQLException {
		// TODO Auto-generated method stub
		
		Connection c = null;
		PreparedStatement ps = null;
		c = ds.getConnection();

		String query = "INSERT INTO users(id, name, password) VALUES (?, ?, ?);";
		ps = c.prepareStatement(query);
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
