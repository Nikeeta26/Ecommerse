package com.example.ecommersebakend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.example.ecommersebakend", "com.ecommerce"})
@EnableJpaRepositories(basePackages = "com.ecommerce.repository")
@EntityScan(basePackages = "com.ecommerce.model")
public class EcommerseBakendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerseBakendApplication.class, args);
    }

}
