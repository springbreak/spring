package org.gradle;

import java.sql.SQLException;

public class Main {

	public static void main(String[] args) throws ClassNotFoundException, SQLException{
		// TODO Auto-generated method stub
		User user = new User();
		
		user.setId("anster");
		user.setPassword("test-pw");
		user.setName("Hoon");
		
		Class.forName("com.mysql.jdbc.Driver");
		
		System.out.println(user.getName());
	}

}
