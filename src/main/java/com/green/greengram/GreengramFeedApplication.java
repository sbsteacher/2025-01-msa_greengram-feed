package com.green.greengram;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class GreengramFeedApplication {
    public static void main(String[] args) {
        SpringApplication.run(GreengramFeedApplication.class, args);
    }
}
