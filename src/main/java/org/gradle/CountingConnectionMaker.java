package org.gradle;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

public class CountingConnectionMaker implements ConnectionMaker {
	
	private int count = 0;
	private ConnectionMaker realConnectionMaker;
	
	public CountingConnectionMaker(ConnectionMaker cm) {
		this.realConnectionMaker = cm;
	}

	@Override
	public Connection getConnection() throws ClassNotFoundException,
			SQLException {
	
		this.count++;
		
		return realConnectionMaker.getConnection();
	}
	
	public int getCount() {
		return this.count;
	}
}
