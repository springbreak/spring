package org.gradle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;

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
