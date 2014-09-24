package lambda.service;

import static lambda.service.UserServiceImpl.MIN_LOGIN_COUNT_FOR_SILVER;
import static lambda.service.UserServiceImpl.MIN_RECOMMEND_COUNT_FOR_GOLD;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import lambda.dao.UserDao;
import lambda.domain.Level;
import lambda.domain.User;
import lambda.util.MockMailSender;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.MailSender;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/test-applicationContext.xml")
public class UserServiceTest {
	
	@Autowired
	ApplicationContext context;

	@Autowired
	MailSender mailSender;

	@Autowired
	UserServiceImpl userService;

	@Autowired
	UserDao userDao;

	@Autowired
	DataSource dataSource;

	@Autowired
	PlatformTransactionManager transactionManager;

	List<User> users;

	@Before
	public void setUp() {
		users = Arrays
				.asList(new User("basic1", "name1", "p1", Level.BASIC,
						MIN_LOGIN_COUNT_FOR_SILVER - 1, 0, "basic1@service.com"),
						new User("basic2", "name2", "p2", Level.BASIC,
								MIN_LOGIN_COUNT_FOR_SILVER, 0,
								"basic2@service.com"), new User("silver1",
								"name3", "p3", Level.SILVER, 60,
								MIN_RECOMMEND_COUNT_FOR_GOLD - 1,
								"silver1@service.com"), new User("silver2",
								"name4", "p4", Level.SILVER, 59,
								MIN_RECOMMEND_COUNT_FOR_GOLD,
								"silver2@service.com"), new User("silver3",
								"name5", "p5", Level.SILVER, 60,
								MIN_RECOMMEND_COUNT_FOR_GOLD,
								"silver3@service.com"), new User("gold",
								"name6", "p6", Level.GOLD, 100, 100,
								"gold@service.com"));
	}

	@Test
	public void add() {
		userDao.deleteAll();

		User userWithLevel = users.get(4);
		User userWithoutLevel = users.get(0);
		userWithoutLevel.setLevel(null);

		userService.add(userWithLevel);
		userService.add(userWithoutLevel);

		assertThat(userWithLevel.getLevel(), is(userWithLevel.getLevel()));
		assertThat(userWithoutLevel.getLevel(), is(Level.BASIC));
	}

	@Test
	@DirtiesContext
	public void upgradeLevels() throws Exception {
		userDao.deleteAll();

		for (User u : users) {
			userDao.add(u);
		}

		MockMailSender mockMailSender = new MockMailSender();
		userService.setMailSender(mockMailSender);
		userService.upgradeLevels();

		checkLevel(users.get(0), false);
		checkLevel(users.get(1), true);
		checkLevel(users.get(2), false);
		checkLevel(users.get(3), true);
		checkLevel(users.get(4), true);
		checkLevel(users.get(5), false);

		List<String> request = mockMailSender.getRequests();
		assertThat(request.size(), is(3));
		assertThat(request.get(0), is(users.get(1).getEmail()));
		assertThat(request.get(1), is(users.get(3).getEmail()));
		assertThat(request.get(2), is(users.get(4).getEmail()));
	}

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	@DirtiesContext
	public void upgradeAllOrNothing() throws Exception {

		userDao.deleteAll();

		String upgradeStopPositionId = users.get(3).getId();

		UserServiceImpl testUserService = new TestUserService(upgradeStopPositionId);
		testUserService.setUserDao(this.userDao);
		testUserService.setMailSender(mailSender);

		TxProxyFactoryBean txProxyFactoryBean = (TxProxyFactoryBean)
				context.getBean("&userService", TxProxyFactoryBean.class);
		
		txProxyFactoryBean.setTarget(testUserService);
		UserService txUserService = (UserService) txProxyFactoryBean.getObject();
		
		// below code will not work. 
		// because 'target' should be 'testUserService`
		// UserService txUserService = 
		// (UserService) context.getBean("userService");

		for (User u : users) {
			userDao.add(u);
		}

		exception.expect(TestUserServiceException.class);
		txUserService.upgradeLevels();

		checkLevel(users.get(1), false);
	}

	private void checkLevel(User user, boolean upgraded) {
		User userUpgraded = userDao.get(user.getId());

		if (upgraded) {
			assertThat(userUpgraded.getLevel(), is(user.getLevel().nextLevel()));
		} else {
			assertThat(userUpgraded.getLevel(), is(user.getLevel()));
		}
	}
}
