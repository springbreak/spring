package org.gradle;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class GConnectionMaker implements ConnectionMaker {

	@Override
	public Connection getConnection() throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		Connection c = DriverManager.getConnection(
				"jdbc:mysql://localhost/springbreak", 
				"spring", 
				"break");

		return c;	
	}

}
