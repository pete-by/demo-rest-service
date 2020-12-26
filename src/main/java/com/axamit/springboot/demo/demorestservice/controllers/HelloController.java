package com.axamit.springboot.demo.demorestservice.controllers;

import com.axamit.springboot.demo.demorestservice.UnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

import static org.springframework.boot.availability.LivenessState.CORRECT;

@RestController
class HelloController {

    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    @Inject
    private ApplicationAvailability applicationAvailability;

    private @Value("${env}") String env;

    private final String greeting;

    HelloController(@Value("${greeting:Hi}") String greeting) {
        this.greeting = greeting;
    }

    @GetMapping("/hello")
    String hello(String name) {
        // lets simulate unavailability
        if (!isAlive()) {
            throw new UnavailableException("Service is unavailable");
        }
        return greeting + ", " + name + "!";
    }

    /**
     * @return status
     */
    @GetMapping("/alive")
    String alive() {
        // lets simulate unavailability
        if (!isAlive()) {
            throw new UnavailableException("Service is unavailable");
        }
        return "OK";
    }

    private boolean isAlive() {
        return applicationAvailability.getLivenessState() == CORRECT;
    }

}
