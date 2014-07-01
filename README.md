
## 5.1 사용자 레벨 관리 기능 추가

```mysql
ALTER TABLE users ADD level int not null;
ALTER TABLE users ADD login int not null;
ALTER TABLE users ADD recommend int not null;
```

사용자의 레벨을 업그레이드 하는 `upgradeLevels` 메소드는 어디 위치해야 할까? `User` Class 에 대한 비즈니스 로직을 담당하는 `UserSerivce` 에 위치할 수 있을것 같다.

```java
public void upgradeLevels() {
	List<User> users = userDao.getAll();
	
	for(User u : users) {
		Boolean changed = null;
		
		if (u.getLevel() == Level.BASIC && u.getLogin() >= 50) {
			u .setLevel(Level.SILVER);
			changed = true;
		} else if (u.getLevel() == Level.SILVER && u.getRecommend() >= 30) {
			u.setLevel(Level.GOLD);
			changed = true;
		} else if (u.getLevel() == Level.GOLD) {
			changed = false;
		} else {
			changed = false;
		}
		
		if (changed) {
			userDao.update(u);
		}
	}
}

// Test Code for upgradeLevels()

@Test
public void upgradeLevels() {
  userDao.deleteAll();
   
  for(User u : users) {
    userDao.add(u);
  }
    
  userService.upgradeLevels();
    
  checkLevel(users.get(0), Level.BASIC);
  checkLevel(users.get(1), Level.SILVER);
  checkLevel(users.get(2), Level.SILVER);
  checkLevel(users.get(3), Level.GOLD);
  checkLevel(users.get(4), Level.GOLD);
  checkLevel(users.get(5), Level.GOLD);
  
}

private void checkLevel(User user, Level expectedLevel) {
  User userUpgraded = userDao.get(user.getId());
    
  assertThat(userUpgraded.getLevel(), is(expectedLevel));
}
``` 

신규 사용자가 추가 되었을때, 초기 레벨이 설정되어 있지 않다면, BASIC 으로 세팅해주는 것도 비즈니스 로직을 담당하는 `UserService` 클래스에 추가해 보자.

```java
	public void add(User u) {
	  if (u.getLevel() == null) { 
	    u.setLevel(Level.BASIC);
	  }
	  
	  userDao.add(u);
	}
	
	// Test for UserService.add()
  public void add() {
    userDao.deleteAll();
    
    User userWithLevel = users.get(4);
    User userWithoutLevel = users.get(0);
    userWithoutLevel.setLevel(null);
    
    userService.add(userWithLevel);
    userService.add(userWithoutLevel);
    
    assertThat(userWithLevel.getLevel(), is(userWithLevel.getLevel()));
    assertThat(userWithoutLevel.getLevel(), is(Level.BASIC));
  }
```

이쯤 와서 보니, `UserService.upgradeLevels()` 메소드의 로직이 만족스럽지 않다. 현재 레벨을 검사하는 부분과 업그레이드 조건을 검사하는 부분이 섞여 있고, 새로운 레벨을 추가하면 If 블럭이 늘어나니 버그가 생기기 쉽다. 

**항상 이 코드가 변화에 취약한가 생각해보자.**

또한 `User` 객체의 속성을 변경하는 코드가 `UserService` 에 있기 보다는 `UserService` 가 `User` 에게 속성을 변경해 달라고 요청하는것이 적합하다. 

**항상 이 코드가 여기에 위치해야 하는가 생각해보자.**

`UserService` 는 `User` 에게 업그레이드가 가능한지 검사를 요청하고, 그 결과값을 받아 다시 업그레이드를 요청하도록 하고, `User` 가 직접 다음 단계의 레벨을 가지도록 하지 말고, 그 정보는 `Level` 이 가질 수 있도록 하면 코드는 아래와 같다.

```java

// Level.java
public enum Level {
	GOLD(3, null), SILVER(2, GOLD), BASIC(1, SILVER);

	private final int value;
	private final Level next;

	Level(int value, Level next) {
		this.value = value;
		this.next = next;
	}

	public int intValue() {
		return this.value;
	}
	
	public Level nextLevel() {
	  return this.next;
	}

	public static Level valueOf(int value) {
		switch(value) {
			case 1: return BASIC;
			case 2: return SILVER;
			case 3: return GOLD;
			default: throw new AssertionError("Unknowl value: " + value);
		}
	}
}

// User.java

  public boolean canUpgradeLevel() {
    
    Level currentLevel = this.getLevel();
    
    switch(currentLevel) {
      case BASIC: return (this.getLogin() >= 50);
      case SILVER: return (this.getRecommend() >= 30);
      case GOLD: return false;
      default: throw new IllegalArgumentException("Unknowl Level :" +
          currentLevel);
    }
  }

  public void upgradeLevel() {
    Level nextLevel = this.level.nextLevel();
    
    if (nextLevel == null) {
      throw new IllegalArgumentException(this.level + " 은 업그레이드가 불가능합니다.");
    } else {
      this.level = nextLevel;
    }
    
  }
  
// UserService.java
	public void upgradeLevels() {
	  List<User> users = userDao.getAll();
	  
	  for(User u : users) {
	    if (u.canUpgradeLevel()) {
	      upgradeLevel(u);
	    }
	  }
	  
	}

  private void upgradeLevel(User u) {
    u.upgradeLevel();
    userDao.update(u);
  }
```

레벨이 업그레이드가 될 경우, 로그를 남기거나, 관리자에게 통지하는 등의 작업을 할 수가 있으므로 메소드로 분리하는 편이 낫다. 그리고 한가지 더 변경에 취약한 부분이 있다. `UserService` 에서 레벨 변경 조건을 숫자로 지정한 부분이다.

```java
  public boolean canUpgradeLevel(User u) {
    
    Level currentLevel = u.getLevel();
    
    switch(currentLevel) {
      case BASIC: return (u.getLogin() >= 50);
      case SILVER: return (u.getRecommend() >= 30);
      case GOLD: return false;
      default: throw new IllegalArgumentException("Unknowl Level :" +
          currentLevel);
    }
  }
```

다음과 같이 `UserService` 에 상수로 만들어 주고 사용하자.

```java
// UserService

public class UserService {
  
  public static final int MIN_LOGIN_COUNT_FOR_SILVER = 50;
  public static final int MIN_RECOMMEND_COUNT_FOR_GOLD = 30;
  
  public boolean canUpgradeLevel(User u) {
    
    Level currentLevel = u.getLevel();
    
    switch(currentLevel) {
      case BASIC: return (u.getLogin() >= MIN_LOGIN_COUNT_FOR_SILVER);
      case SILVER: return (u.getRecommend() >= MIN_RECOMMEND_COUNT_FOR_GOLD);
      case GOLD: return false;
      default: throw new IllegalArgumentException("Unknown Level :" +
          currentLevel);
    }
  }
  
  
// UserServiceTest
import static org.gradle.service.UserService.MIN_LOGIN_COUNT_FOR_SILVER;
import static org.gradle.service.UserService.MIN_RECOMMEND_COUNT_FOR_GOLD;

  @Before
  public void setUp() {
    users = Arrays.asList( 
        new User("basic1", "name1", "p1", Level.BASIC, MIN_LOGIN_COUNT_FOR_SILVER-1, 0),
        new User("basic2", "name2", "p2", Level.BASIC, MIN_LOGIN_COUNT_FOR_SILVER, 0),
        new User("silver1", "name3", "p3", Level.SILVER, 60, MIN_RECOMMEND_COUNT_FOR_GOLD-1),
        new User("silver2", "name4", "p4", Level.SILVER, 59, MIN_RECOMMEND_COUNT_FOR_GOLD),
        new User("silver3", "name5", "p5", Level.SILVER, 60, MIN_RECOMMEND_COUNT_FOR_GOLD),
        new User("gold", "name6", "p6", Level.GOLD, 100, 100));
  }  
```

사실 이 정책 부분도 변경이 가능하다. 그럴때 마다 상수값을 수정하거나 추가하는 것이 아니라, UserServicePolicy 와 같은 인터페이스를 만들어 DI 를 해주며 정책을 바꿔가면서 프로그램을 구동할 수 있다.

```java
interface UserServicePolicy {
  public boolean canUpgradeLevel();
  public void upgradeLevel();
}
```



