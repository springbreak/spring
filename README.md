## 1.6 싱글톤 레지스트리와 오브젝트 스코프

- DaoFactory 로 UserDao 를 생성하면, 매번 다른 오브젝트가 만들어진다. 
<br/>
```java
DaoFactory factory = new DaoFactory();
UserDao dao1= factory.userDao();
UserDao dao2= factory.userDao();

System.out.println(dao1);
System.out.println(dao2);

: org.gradle.UserDao@15db9742
: org.gradle.UserDao@6d06d69c

ApplicationContext context = 
	new AnnotationConfigApplicationContext(DaoFactory.class); 
	
UserDao dao3 = context.getBean("userDao", UserDao.class);
UserDao dao4 = context.getBean("userDao", UserDao.class);
		
System.out.println(dao3);
System.out.println(dao4);

: org.gradle.UserDao@e720b71
: org.gradle.UserDao@e720b71
```
<br/>
- **ApplicationContext** 는 DaoFactory 와 비슷한 IoC 컨테이너이지만, 동시에 **ApplicationContext** 는 싱글톤을 저장하고 관리하는 **싱글톤 레지스트리** 이기도 함. 스프링은 기본적으로 별다른 설정을 하지 않으면 내부에서 생성하는 빈 오브젝트를 모두 싱글톤으로 만든다.
<br/><br/>
- 자바에서 싱글톤을 만드는 일반적인 방법은 **Private** 생성자를 이용하는 것. 그러나 몇 가지 문제점이 있다.
  - **Private** 생성자이므로 상속할 수 없다. 
  - 오브젝트 주입이 어려워 테스트하기가 힘들다.
  - 전역 상태를 만드므로 바람직하지 않다.
  - 다수개의 JVM 이 있는 환경이거나, 서버 환경에서 하나만 만들어지는 것을 보장하지 못함.  
<br/><br/>
- 엔터프라이즈 서버 환경에서 싱글톤은 필요하지만, 직접 구현시 문제가 많으므로 스프링은 싱글톤 레지스트리로서의 기능도 제공한다. 그리고 이때 **Static** 메소드와 **Private** 생성자가 아닌 평범한 클래스도 싱글톤으로 활용하게 도와준다. 

## equals() 와 hashcode()

- **equals** 는 두 객체의 내용이 같은지, 동등성(equality) 를 비교하는 연산자
- **hashCode** 는 두 객체가 같은 객체인지, 동일성(identity) 를 비교하는 연산자
<br/><br/>
다음과 같은 클래스가 있을때 **equals()** 를 이용한 두 객체의 동등성 비교는 올바른 결과가 나오지 않는다.
<br/>
```java
public class Person {
	private int id;
	private String name;

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}


Person p1 = new Person();
p1.setId(1);
p1.setName("Sam");
		
Person p2 = new Person();
p2.setId(1);
p2.setName("Sam");
		
System.out.println(p1.equals(p2));

// false
```
<br/>
따라서 **eqauls()** 를 오버라이딩 해 올바른 결과를 돌려줄 수 있도록 해야한다.
<br/>
```java
@Override
public boolean equals(Object obj) {

		if (obj == null) {
			return false;
		}
		
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		
		if (this == obj) {
			return true;
		}
		
		Person that = (Person) obj;
		
		if (this.name == null && that.name != null) {
			return false;
		}
		
		if (this.id == that.id && this.name.equals(that.name)) {
			return true;
		}
		
		return false;
	}
```
<br/>
하지만 **HashMap** 이나 **HashSet**, **HashTable** 과 같은 객체들을 사용하는 경우, 객체의 의미상의 동등성 비교를 위해 **hashCode()** 를 호출한다.
<br/>
```java
Map<Person, Integer> map = new HashMap<Person, Integer>();
map.put(p1, 1);
map.put(p2, 1);
System.out.println(map.size());

// 2
```
<br/>  
의미상으로 `p1`, `p2` 는 같은 객체이며 **equals()** 까지 오버라이딩 했으나, **HashMap** 이 **hashCode()** 를 이용해 두 객체가 동등한지 비교하므로 기대했던 대로 작동하지 않는다. **hashCode()** 를 오버라이딩 해야함
<br/>
```java
@Override
public int hashCode() {

	final int prime = 31;
	int result = 1;
	
	result = prime * result + ((name == null) ? 0 : name.hashCode());
	result = prime * result + id;

	return result;
}
```
<br/>
그러나 여전히 문제가 되는 부분이 있다. 다음의 코드를 보자. 
<br/>
```java
Map<Person, Integer> map = new HashMap<Person, Integer>();
	
Person p1 = new Person();
p1.setId(1);
p1.setName("Kally");
	
Person p2 = new Person();
p2.setId(1);
p2.setName("Sam");
	
map.put(p1, 1);
map.put(p2, 1);
	
p1.setName("Sam");
	
System.out.println(map.size());

// 2
```
<br/>
**Map** 은 **put()** 하는 순간에 들어오는 오브젝트의 **hashCode()** 값을 기억하고 있으므로, `name` 이 변경된 뒤의 **hashCode()** 값을 인식하지 못한다.
<br><br/>
그러므로 **equals() 나 hashCode() 에서 비교하는 멤버로 immutable을 사용할 수 없다면, mutable 한 멤버는 비교의 대상으로 사용해서는 안됌.**

<br/>
### 3.1 equals() 와 관련된 규약

1. Reflexive : Object must be equal to itself.
<br/>
2. Symmetric : if a.equals(b) is true then b.equals(a) must be true.
<br/>
3. Transitive : if a.equals(b) is true and b.equals(c) is true then c.equals(a) must be true.
<br/>
4. Consistent : multiple invocation of equals() method must result same value until any of properties are modified. So if two objects are equals in Java they will remain equals until any of there property is modified.
<br/>
5. Null comparison : comparing any object to null must be false and should not result in NullPointerException. For example a.equals(null) must be false, passing unknown object, which could be null,  to equals in Java is is actually a Java coding best practice to avoid NullPointerException in Java.


<br/>
### 3.2 hashCode() 와 관련된 규약
1. **equals()** 로 비교시 두개의 오브젝트가 같다면, **hashCode()** 값도 같아야 한다.
<br/>
2. **equals()** 로 비교시 **false** 라면, **hashCode()** 값은 다를수도, 같을수도 있다. 그러나 성능을 위해서는 **hashCode()** 값이 다른것이 낫다. 그래야 해싱 알고리즘으로 **Set** 에 해당 오브젝트가 존재하는지 아닌지 빠르게 검색할 수 있다.
<br/>
3. **hashCode()** 값이 같다고 해서, **eqauls()** 가 **true** 를 리턴하는 것은 아니다. 해싱 알고리즘 자체의 문제로, 같은 해시값이 나올 수 있다. 
 
<br/>
### 3.3 References
1. http://www.learn-about-linux.com/2014/05/overriding-equals-and-hashcode-method.html
2. http://iilii.egloos.com/4000476
