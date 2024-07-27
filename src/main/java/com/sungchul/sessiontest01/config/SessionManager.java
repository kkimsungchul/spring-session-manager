package com.sungchul.sessiontest01.config;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;


import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
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
