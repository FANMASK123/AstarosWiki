package com.example.astaroswiki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AstarosWikiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AstarosWikiApplication.class, args);
    }

}
