package org.gradle.dao;

import java.util.List;

import org.gradle.domain.User;

public interface UserDao {
	public void deleteAll();

	public int getCount();
	
	public void update(User user);

	public void add(User user) throws DuplicationUserIdException;

	public User get(String id);

	public List<User> getAll();
}
