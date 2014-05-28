package org.gradle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class GUserDao extends UserDao {

	@Override
	public Connection getConnection() throws ClassNotFoundException,
			SQLException {
		// TODO Auto-generated method stub
		Class.forName("com.mysql.jdbc.Driver");
		Connection c = DriverManager.getConnection(
				"jdbc:mysql://localhost/springbreak", 
				"spring", 
				"break");
		
		return c;
	}

}
