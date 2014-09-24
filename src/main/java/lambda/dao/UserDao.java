package lambda.dao;

import java.util.List;

import lambda.domain.User;

public interface UserDao {
	public void deleteAll();

	public int getCount();
	
	public void update(User user);

	public void add(User user) throws DuplicationUserIdException;

	public User get(String id);

	public List<User> getAll();
}
