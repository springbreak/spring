package org.gradle;

import java.sql.SQLException;

public class UserDaoTest {

	public static void main(String[] args) throws ClassNotFoundException, SQLException{

		User user = new User();
		
		user.setId("anster");
		user.setName("Hoon");
		user.setPassword("test-pw");
		
		ConnectionMaker cm= new GConnectionMaker();
		UserDao dao = new UserDao(cm);
		dao.add(user);
		
		User user2 = dao.get(user.getId());
		System.out.println(user2.getName());
		
		System.out.println(user.getName());
	}
}
