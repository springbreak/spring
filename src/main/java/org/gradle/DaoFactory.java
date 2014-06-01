package org.gradle;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DaoFactory {

	@Bean
	public UserDao userDao() {
		// TODO Auto-generated method stub
		ConnectionMaker cm = getConnectionMaker();
		UserDao dao = new UserDao();
		dao.setConnectionMaker(cm);
		return dao;
	}

	@Bean
	public ConnectionMaker getConnectionMaker() {
		return new GConnectionMaker();
	}

}
