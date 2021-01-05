package com.axamit.springboot.demo.demorestservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoRestServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(DemoRestServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(DemoRestServiceApplication.class, args);
    }

}