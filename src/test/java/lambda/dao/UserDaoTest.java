package lambda.dao;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import lambda.dao.DuplicationUserIdException;
import lambda.dao.UserDao;
import lambda.domain.Level;
import lambda.domain.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/test-applicationContext.xml")
public class UserDaoTest {
	
	@Autowired
	private UserDao dao;
	
	@Autowired
	private DataSource ds;
	
	// Fixture
	private User user1;
	private User user2;
	private User user3;
	
	@Before
	public void setUp() {
		user1 = new User("first", "il", "pw1", Level.BASIC, 1, 0, "first@service.com");
		user2 = new User("second", "e", "pw2", Level.SILVER, 55, 10, "second@service.com");
		user3 = new User("third", "sam", "pw3", Level.GOLD, 100, 40, "third@service.com");
	}
	
	@Test
	public void update() {
		dao.deleteAll();
		
		dao.add(user1);
		dao.add(user2);
		
		user1.setName("changedName");
		user1.setPassword("123132");
		user1.setLevel(Level.GOLD);
		user1.setLogin(1000);;
		user1.setRecommend(999);
		
		dao.update(user1);
		
		User user1updated = dao.get(user1.getId());
		checkSameUser(user1, user1updated);
		
		User user2same = dao.get(user2.getId());
		checkSameUser(user2, user2same);
	}
	
	@Test
	public void addAndGet() throws ClassNotFoundException, SQLException{

		dao.deleteAll();
		assertThat(dao.getCount(), is(0));

		dao.add(user1);
		assertThat(dao.getCount(), is(1));
		
		User userget1 = dao.get(user1.getId());
		checkSameUser(user1, userget1);
	}
	
	@Test(expected=EmptyResultDataAccessException.class)
	public void getUserFailure() throws SQLException {
		
		dao.deleteAll();
		
		assertThat(dao.getCount(), is(0));
		
		dao.get("unknowl_id");
	}
	
	@Test(expected=DuplicationUserIdException.class)
	public void addUserFailure() {
		
		dao.deleteAll();
		
		dao.add(user1);
		dao.add(user1);
	}
	
	@Test
	public void getAllTest() throws SQLException {
		dao.deleteAll();
		
		List<User> users0 = dao.getAll();
		assertThat(users0.size(), is(0));
	
		
		dao.add(user1);
		List<User> users1 = dao.getAll();
		assertThat(users1.size(), is(1));
		checkSameUser(user1, users1.get(0));
		
		dao.add(user2);
		List<User> users2 = dao.getAll();
		assertThat(users2.size(), is(2));
		checkSameUser(user1, users2.get(0));
		checkSameUser(user2, users2.get(1));
		
		dao.add(user3);
		List<User> users3 = dao.getAll();
		assertThat(users3.size(), is(3));
		checkSameUser(user1, users3.get(0));
		checkSameUser(user2, users3.get(1));
		checkSameUser(user3, users3.get(2));
	}

	public void checkSameUser(User u1, User u2) {
		assertThat(u1.getId(), is(u2.getId()));
		assertThat(u1.getName(), is(u2.getName()));
		assertThat(u1.getPassword(), is(u2.getPassword()));
		assertThat(u1.getLevel(), is(u2.getLevel()));
		assertThat(u1.getLogin(), is(u2.getLogin()));
		assertThat(u1.getRecommend(), is(u2.getRecommend()));
	}
}
