package com.example.StudentSystemSpring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@SpringBootApplication
public class StudentSystemSpringApplication {
	public static void main(String[] args) {
		SpringApplication.run(StudentSystemSpringApplication.class, args);
	}
}
