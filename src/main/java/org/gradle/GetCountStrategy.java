package org.gradle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class GetCountStrategy implements StatementStrategy {

	@Override
	public PreparedStatement createStatement(Connection c) throws SQLException {
	
		PreparedStatement ps = c.prepareStatement("select count(*) from users");
		return ps;
	}

}
