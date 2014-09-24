package lambda.service;

import java.util.List;

import lambda.dao.UserDao;
import lambda.domain.Level;
import lambda.domain.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

public class UserServiceImpl implements UserService {

	public static final int MIN_LOGIN_COUNT_FOR_SILVER = 50;
	public static final int MIN_RECOMMEND_COUNT_FOR_GOLD = 30;

	@Autowired
	private MailSender mailSender;

	public void setMailSender(MailSender ms) {
		this.mailSender = ms;
	}

	@Autowired
	private UserDao userDao;

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	@Override
	public void add(User u) {
		if (u.getLevel() == null) {
			u.setLevel(Level.BASIC);
		}

		userDao.add(u);
	}

	@Override
	public void upgradeLevels() {

		List<User> users = userDao.getAll();

		for (User u : users) {
			if (canUpgradeLevel(u)) {
				upgradeLevel(u);
			}
		}
	}

	public boolean canUpgradeLevel(User u) {

		Level currentLevel = u.getLevel();

		switch (currentLevel) {
		case BASIC:
			return (u.getLogin() >= MIN_LOGIN_COUNT_FOR_SILVER);
		case SILVER:
			return (u.getRecommend() >= MIN_RECOMMEND_COUNT_FOR_GOLD);
		case GOLD:
			return false;
		default:
			throw new IllegalArgumentException("Unknowl Level :" + currentLevel);
		}
	}

	protected void upgradeLevel(User u) {
		u.upgradeLevel();
		userDao.update(u);
		sendUpgradeEmail(u);
	}

	private void sendUpgradeEmail(User u) {
		// send email
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(u.getEmail());
		message.setFrom("admin@service.com");
		message.setSubject("Level Upgraded");
		message.setText("your level is upgraded to " + u.getLevel());

		mailSender.send(message);
	}
}
