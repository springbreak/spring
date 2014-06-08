package org.gradle;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;

public class UserDaoTest {
	
	private UserDao dao;
	
	@Before
	public void setUp() {

		ApplicationContext context =
				new GenericXmlApplicationContext("applicationContext.xml");
		
		this.dao = context.getBean("userDao", UserDao.class);
	}
	
	@Test
	public void addAndGet() throws ClassNotFoundException, SQLException{

		
		User user = new User();
		user.setId("anster");
		user.setName("Hoon");
		user.setPassword("test-pw");
		
		dao.deleteAll();
		assertThat(dao.getCount(), is(0));

		dao.add(user);
		assertThat(dao.getCount(), is(1));
		
		User user2 = dao.get(user.getId());
		assertThat(user2.getName(), is(user.getName()));
	}
	
	@Test(expected=EmptyResultDataAccessException.class)
	public void getUserFailure() throws SQLException {
		
		dao.deleteAll();
		
		assertThat(dao.getCount(), is(0));
		
		dao.get("unknowl_id");
	}
}
