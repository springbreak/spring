
## 3.2 JDBC 예외 처리

현재 만든 코드는 예외처리가 되어있지 않아 커넥션 풀을 반환하지 않고, `c.close()` 호출하지 않고 종료될 가능성이 있다. 그러나 JDBC 코드에 try/catch/finally 를 단순하게 적용하면 ~~암걸릴것같은~~ 무지막지한 코드가 생성된다.  

```java
// finally block 만 이만큼


finally {            
   if (rs != null) {
        try {
            rs.close();
        } catch (SQLException e) {
         LOG.warn("Failed to close rs", e);
        }
    }
    if (st != null) {
        try {
            st.close();
        } catch (SQLException e) { 
         LOG.warn("Failed to close st", e);     
        }
    }
    if (conn != null) {
        try {
            conn.close();
        } catch (SQLException e) {
         LOG.warn("Failed to close conn", e);
        }
    }
}
```

모든 JDBC 액션(CRUD) 에 이런 코드를 삽입할 순 없다. 중복도 문제고, 어느 한 곳에서 실수로 close 를 흘리면 찾기도 어렵다. 해결방법을 모색해 보자. 가장 먼저 해야 할 일은 변하지 않는 부분 (try/catch) 과 변하는 부분(query) 을 분리해야 한다.

1. 변하지 않는 부분을 그대로 내버려 두고, 변하는 부분 (Query) 를 메소드화 한다. !?! 이상하다 중복되는 부분이 메소드화 되어야 하는데
2. 그래도 일단 템플릿 메소드 패턴을 적용한다. DAO 를 상속하고 변하는 부분을 오버라이딩 해서 CRUD 각각 마다 클래스를 만든다. UserDaoDeleteAll, UserDaoGetCount... ?!? 파일이 몇개가 생기는거야?

 사실은 전략 패턴을 적용하는게 더 낫다. 다음 장에서 알아본다.