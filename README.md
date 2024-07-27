# SpringBoot 세션 관리

## 목차
- [SpringBoot 세션 관리](#SpringBoot-세션-관리)
    * [목적](#목적)
    * [구현 방법](#구현-방법)
    * [구현 클래스](#구현-클래스)
        + [SessionListener 클래스](#sessionlistener-클래스)
            - [SessionListener 내 메서드](#sessionlistener-내-메서드)
        + [SessionManager 클래스](#sessionmanager-클래스)
            - [SessionManager 내 메서드](#sessionmanager-내-메서드)
        + [MainController 클래스](#maincontroller-클래스)
            - [MainController 내 메서드](#maincontroller-내-메서드)
            - [removeSession 과 removeAttribute 의 차이](#removesession-과-removeattribute-의-차이)


## 목적
- 사용자의 세션을 관리
- 세션 ID로 해당 세션에 있는 데이터를 가져오기 위해 구현함

## 구현 방법
- SessionListener 클래스 구현
- SessionManager 클래스 구현
- MainController 클래스 구현

## 구현 클래스

### SessionListener 클래스
- SessionListener 클래스는 HttpSessionListener 인터페이스를 구현함
- 세션의 생성과 소멸을 관리함
- 해당 클래스는 직접 호출하는 부분이 없으며 컴포넌트로 등록하여 스프링 컨테이너에서 관리함

#### SessionListener 내 메서드
```java
@Component
public class SessionListener implements HttpSessionListener {
    
    @Autowired
    private SessionManager sessionManager;
    
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        sessionManager.addSession(se.getSession());
    }
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        System.out.println("## call sessionDestroyed");
        sessionManager.removeSession(se.getSession().getId());
    }
}

```
```text
코드가 길어짐을 방지하기 위해 import , package 부분은 지웠으며, 해당 내용은 직접 코드에 가서 확인
```
- void sessionCreated(HttpSessionEvent se)
  - 사용자가 처음 접속하거나 요청이 들어 올 경우 sessionCreated 메서드를 호출하도록 되어 있음
  - 해당 메서드는 직접 호출하지 않고 Spring servlet container에 의해 자동 호출됨
  - 해당 메서드가 호출 될 때 sessionManager.addSession(se.getSession()); 를 호출하여 ConcurrentHashMap에 session을 저장하도록 하였음


- void sessionDestroyed(HttpSessionEvent se)
  - 사용자의 세션이 종료되거나 만료되면 sessionDestroyed 메서드를 호출함
  - 해당 메서드도 Spring servlet container에 의해 자동 호출됨
  - 해당 메서드가 호출 될 때 sessionManager.removeSession(se.getSession().getId()); 를 호출하여 ConcurrentHashMap에 저장된 session 정보를 삭제하도록 하였음



### SessionManager 클래스
- SessionManager 클래스는 접속한 사용자의 세션을 관리함
- 단일 스레드 환경이 아닌 멀티스레드 환경이므로 ConcurrentHashMap 을 사용함
  - ConcurrentHashMap 과 HashMap의 가장 큰 차이점은 Thread Safe임 
  - Spring은 멀티스레드이므로 HashMap를 사용할 경우 안정성이 보장되지 않음
  - HashMap을 사용하려고 한다면 동기화를 해줘야하는데, 굳이 ConcurrentHashMap을 사용하지 않고 동기화를 구현할 이유가 없음
  - 참고링크 : 
    - https://peonyf.tistory.com/entry/JAVA-HashMap%EC%99%80-Enum
    - https://velog.io/@twinsgemini/%EB%8F%99%EC%8B%9C%EC%84%B1-%EC%9D%B4%EC%8A%88-HashMap-vs-ConcurrentHashMap

#### SessionManager 내 메서드
```java
@Component
public class SessionManager {

    private final Map<String, HttpSession> sessions = new ConcurrentHashMap<>();

    public void addSession(HttpSession session) {
        sessions.put(session.getId(), session);
    }

    public HttpSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public void removeSession(String sessionId) {
        System.out.println("## call removeSession");
        sessions.remove(sessionId);
    }

    public void removeAttribute(String sessionId){
        HttpSession session = getSession(sessionId);
        if (session != null) {
            Enumeration<String> attributeNames = session.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String attributeName = attributeNames.nextElement();
                session.removeAttribute(attributeName);
            }
        }
    }

    public Map<String, Object> getAllAttributes(String sessionId){
        HttpSession session = getSession(sessionId);
        Map<String, Object> attributesMap = new HashMap<>();
        if (session != null) {
            Enumeration<String> attributeNames = session.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String attributeName = attributeNames.nextElement();
                attributesMap.put(attributeName, session.getAttribute(attributeName));
            }
            attributesMap.put("message","Session found");
        }else{
            attributesMap.put("message", "Session not found");
        }
        return attributesMap;
    }
}
```
```text
코드가 길어짐을 방지하기 위해 import , package 부분은 지웠으며, 해당 내용은 직접 코드에 가서 확인
```

- void addSession(HttpSession session)
  - 세션이 생성 될 때 map 에 사용자의 세션ID를 키로, 세션을 value로 추가함 
 
- HttpSession getSession(String sessionId)
  - session id로 해당 세션 정보를 가져옴
  - HttpSession 클래스가 리턴 값이며, Map에 담겨있는 session을 꺼내서 리턴함
 
- void removeSession(String sessionId)
  - session을 map 자체에서 삭제함
 
- void removeAttribute(String sessionId)
  - session에 저장되어 있는 속성(attribute)를 전부 삭제함 

- Map<String, Object> getAllAttributes(String sessionId)
  - session에 저장되어 있는 속성(attribute)들을 전부 꺼내와서 map에 담아 리턴함 

### MainController 클래스
- SessionManager 클래스를 직접 사용하는 예시를 작성한 클래스
- RestController 로 구현하였음
- 간단한 예시이므로 서비스는 구현하지 않았음

#### MainController 내 메서드

```java
@RestController
@RequestMapping("/")
public class MainController {

    @Autowired
    private SessionManager sessionManager;

    @GetMapping("")
    public String createUserSession(HttpServletRequest request){
        HttpSession session = request.getSession();
        String sessionId = session.getId();
        session.setAttribute("userId",sessionId.substring(3,10));
        session.setAttribute("ssoCheck",true);
        session.setAttribute("userIp",request.getRemoteAddr());
        session.setAttribute("UserAgent",request.getHeader("User-Agent"));
        session.setAttribute("loginTime",LocalDateTime.now());
        return sessionId;
    }

    @GetMapping("/get/{sessionId}")
    public Map<String, Object> getUserSessionAttributes(@PathVariable String sessionId) {
        Map<String, Object> attributesMap = sessionManager.getAllAttributes(sessionId);
        //sessionManager.removeSession(sessionId);
        sessionManager.removeAttribute(sessionId);
        return attributesMap;
    }
}
```
```text
코드가 길어짐을 방지하기 위해 import , package 부분은 지웠으며, 해당 내용은 직접 코드에 가서 확인
```
- String createUserSession(HttpServletRequest request)
  - 사용자가 접속 시 session 에 데이터를 담고, 데이터가 담긴 session id 를 리턴하도록 구현하였음
  - 필요한 정보가 더 있다면 추가할 수 있음

- Map<String, Object> getUserSessionAttributes(@PathVariable String sessionId)
  - 입력받은 session id로 session 을 찾아 해당 session에 저장된 데이터(attribute)를 리턴하도록 구현하였음
  - sessionManager.removeSession 를 사용하지 않고 sessionManager.removeAttribute를 사용해야 함

#### removeSession 과 removeAttribute 의 차이
- removeSession : map에 저장되어 있는 세션을 삭제함
```text
removeSession는 session 자체를 삭제하는 것이 아닌 map에 담겨 있는 session 정보를 삭제함
사용자의 세션이 살아있는(유효한) 상태에서는 서블릿 컨테이너에서 HttpSessionListener클래스의 sessionCreated 메서드를 다시 호출하지 않으므로
removeSession 를 사용해서 사용자의 세션을 map에서 지워버렸을 경우 다시 직접 사용자의 세션을 다시 추가 해줘야함
```
- removeAttribute : 해당 session에 저장되어 있는 속성들을 삭제함
```text
removeAttribute 는 map 저장된 세션은 그대로 두면서 해당 세션에 저장되어 있는 데이터(attribute)만을 제거함
사용자가 다시 한번 해당 세션에 데이터를 추가해야 할 상황이 발생할 경우 map에서 지워버리면 더이상 해당 세션을 찾을 방법이 없어짐
그래서 단순히 세션에 저장된 데이터만을 제거하도록 함
```
- removeSession 을 사용했을 경우
```text
1. 사용자가 접속
2. 사용자의 정보를 session에 추가
3. 타 서비스(was)에서 해당 사용자의 session id를 사용하여 세션에 저장되어 있는 정보를 가져간 뒤 removeSession 을 호출하여 해당 세션을 Map에서 삭제
4. 사용자가 다시 해당 페이지에 접속
5. Spring servlet container 에서는 이미 사용자의 세션이 생성되어 있으므로 sessionCreated() 메서드를 호출하지 않음
※ sessionCreated() 메서드가 호출되지 않으면 세션의 정보를 저장하는 map에도 세션 정보가 다시 저장되지 않음
6. 타 서비스에서 해당 사용자의 session id로 호출 시 map에는 데이터가 없기때문에 Session not found 리턴
```
- removeAttribute 을 사용했을 경우
```text
1. 사용자가 접속
2. 사용자의 정보를 session에 추가
3. 타 서비스(was)에서 해당 사용자의 session id를 사용하여 세션에 저장되어 있는 정보를 가져간 뒤 removeAttribute 을 호출하여 해당 세션에 저장되어 있는 데이터(attribute)를 삭제
4. 사용자가 다시 해당 페이지에 접속
5. sessionManager 클래스에서 관리하는 map에는 해당 사용자의 session id가 존재하므로, session 정보를 가져와서 데이터 추가
6. 타 서비스에서 해당 사용자의 session id로 호출 시 저장된 데이터 return
```