package org.gradle;

public class DaoFactory {

	public UserDao userDao() {
		// TODO Auto-generated method stub
		ConnectionMaker cm = getConnectionMaker();
		UserDao dao = new UserDao(cm);
		return dao;
	}
	
	private ConnectionMaker getConnectionMaker() {
		return new GConnectionMaker();
	}

}
