package com.sungchul.sessiontest01.config;

import jakarta.servlet.http.HttpSession;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
        //sessions.get(sessionId).invalidate();
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

    // map에 들어 있는 모든 데이터는 삭제 했지만 서블릿컨테이너에서 관리하는 세션은 삭제하지 않았음
    // 스케줄러가 돌고 난 뒤에 해당 세션의  ID로 데이터를 찾으려고 하면, map에서만 삭제되어서 찾지 못하는 상황이 발생함
    // 그래서 invalidate() 도 같이 진행해야 함
    @Scheduled(fixedRate = 3600000) // 1시간 마다 실행
    public void cleanupInactiveSessions() {
        long now = System.currentTimeMillis();
        sessions.entrySet().removeIf(entry -> {
            HttpSession session = entry.getValue();
            boolean isInactive = now - session.getLastAccessedTime() > session.getMaxInactiveInterval() * 1000;
            if (isInactive) {
                session.invalidate();
                System.out.println("Session invalidated: " + session.getId());
            }
            return isInactive;
        });
        System.out.println("### clear inactive sessions (ConcurrentHashMap)");
    }

    @Scheduled(fixedRate = 60000)   //60초마다 모든 세션 삭제
    public void cleanupSessions() {
        for (HttpSession session : sessions.values()) {
            session.invalidate();
        }
        sessions.clear();
        System.out.println("All sessions cleared at " + java.time.LocalDateTime.now());
    }

    @Scheduled(cron = "0 0 5 * * ?")  // 매일 아침 5시에 저장된 모든 세션 삭제
    public void cleanupAllSessions() {
        for (HttpSession session : sessions.values()) {
            session.invalidate();
        }
        sessions.clear();
        System.out.println("All sessions cleared at " + java.time.LocalDateTime.now());
    }
}
