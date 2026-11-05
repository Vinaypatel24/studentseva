package com.studentseva;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * StudentSeva Main Application Entry Point.
 * 
 * Note: DataSourceAutoConfiguration can run once database credentials are provided in Day 2.
 */
@SpringBootApplication
@EnableScheduling
public class StudentSevaApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentSevaApplication.class, args);
    }
}

