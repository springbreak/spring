package org.gradle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;
import javax.swing.tree.TreePath;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

public class UserDao {

	@Autowired
	private JdbcContext jdbcContext;

	@Autowired
	private JdbcTemplate jt;

	@Autowired
	private DataSource ds;

	public void deleteAll() {
		jt.update("delete from users");
	}

	public int getCount() {
		return jt.queryForObject("select count(*) from users", Integer.class);
	}

	public void add(User user) {
		jt.update("INSERT INTO users(id, name, password) VALUES (?, ?, ?);",
				user.getId(), user.getName(), user.getPassword());
	}

	public User get(String id) {
		return jt.queryForObject(
				"SELECT * from users WHERE id = ?",
				new Object[] {id},
				(rs, rowNum) -> {
					User user = new User();
					user.setId(rs.getString("id"));
					user.setName(rs.getString("name"));
					user.setPassword(rs.getString("password"));
					return user;
				});
	}

	public List<User> getAll() {
		
		return jt.query(
				"SELECT * FROM users",
				(rs, rowNum) -> {
					return new User(rs.getString(1), rs.getString(2), rs.getString(3));
				});
	}
	
}
