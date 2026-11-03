package com.example.travelplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TravelPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(TravelPlatformApplication.class, args);
    }
}

