package lambda.service;

import lambda.domain.User;

public interface UserService {

	public abstract void add(User u);

	public abstract void upgradeLevels();

}