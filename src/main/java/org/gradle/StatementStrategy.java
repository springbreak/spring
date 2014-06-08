package org.gradle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface StatementStrategy {	
	public PreparedStatement createStatement(Connection c) throws SQLException;
}
