package com.example.intellierp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IntelliErpApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntelliErpApplication.class, args);
    }
}
