package com.sungchul.sessiontest01.controller;


import com.sungchul.sessiontest01.config.SessionManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


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
