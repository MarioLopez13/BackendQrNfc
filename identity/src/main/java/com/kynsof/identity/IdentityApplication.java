package com.kynsof.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class IdentityApplication {
    // Starting Identity Application
    public static void main(String[] args) {
        SpringApplication.run(IdentityApplication.class, args);
    }

}