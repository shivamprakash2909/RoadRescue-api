package com.shivam.roadrescue.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.shivam.roadrescue.customer", "com.shivam.roadrescue.shared"})
@EntityScan(basePackages = {"com.shivam.roadrescue.customer.entity"})
@EnableJpaRepositories(basePackages = {"com.shivam.roadrescue.customer.repository"})
public class CustomerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
    }
}

