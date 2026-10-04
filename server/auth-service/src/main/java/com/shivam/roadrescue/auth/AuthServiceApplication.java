package com.shivam.roadrescue.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.shivam.roadrescue.auth", "com.shivam.roadrescue.shared"})
@EntityScan(basePackages = {"com.shivam.roadrescue.auth.entity"})
@EnableJpaRepositories(basePackages = {"com.shivam.roadrescue.auth.repository"})
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
