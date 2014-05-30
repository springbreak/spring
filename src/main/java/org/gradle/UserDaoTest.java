package org.gradle;

import java.sql.SQLException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class UserDaoTest {

	public static void main(String[] args) throws ClassNotFoundException, SQLException{

//		User user = new User();
//		
//		user.setId("anster");
//		user.setName("Hoon");
//		user.setPassword("test-pw");
//		
//		
//		
//		dao.add(user);
//		
//		User user2 = dao.get(user.getId());
//		System.out.println(user2.getName());
//		
//		System.out.println(user.getName());
		
		DaoFactory factory = new DaoFactory();
		UserDao dao1= factory.userDao();
		UserDao dao2= factory.userDao();
		
		System.out.println(dao1);
		System.out.println(dao2);

		ApplicationContext context =
				new AnnotationConfigApplicationContext(DaoFactory.class);

		UserDao dao3 = context.getBean("userDao", UserDao.class);
		UserDao dao4 = context.getBean("userDao", UserDao.class);
		
		System.out.println(dao3);
		System.out.println(dao4);
	}
}
