package org.gradle;

import java.util.List;

public interface UserDao {
	public void deleteAll();

	public int getCount();

	public void add(User user) throws DuplicationUserIdException;

	public User get(String id);

	public List<User> getAll();
}
