package com.sungchul.sessiontest01;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SessionTest01Application {

    public static void main(String[] args) {
        SpringApplication.run(SessionTest01Application.class, args);
    }
}
