package org.gradle;

import java.sql.SQLException;

import org.junit.Test;
import static org.junit.Assert.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import static org.hamcrest.CoreMatchers.*;

public class UserDaoTest {
	
	@Test
	public void addAndGet() throws ClassNotFoundException, SQLException{

		User user = new User();
		
		user.setId("anster");
		user.setName("Hoon");
		user.setPassword("test-pw");
		
		ApplicationContext context =
				new GenericXmlApplicationContext("applicationContext.xml");
		
		UserDao dao = context.getBean("userDao", UserDao.class);
		dao.add(user);
		
		User user2 = dao.get(user.getId());
		
		assertThat(user2.getName(), is(user.getName()));
	}
}
