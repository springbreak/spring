package org.gradle;

import java.sql.SQLException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

public class UserDaoTest {

	public static void main(String[] args) throws ClassNotFoundException, SQLException{

		User user = new User();
		
		user.setId("anster");
		user.setName("Hoon");
		user.setPassword("test-pw");
		
		ApplicationContext context =
				new GenericXmlApplicationContext("applicationContext.xml");
		
		UserDao dao = context.getBean("userDao", UserDao.class);
		dao.add(user);
		
		User user2 = dao.get(user.getId());
		System.out.println(user2.getName());
	}
}
