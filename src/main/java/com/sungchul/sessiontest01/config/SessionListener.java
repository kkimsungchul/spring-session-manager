package com.sungchul.sessiontest01.config;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
