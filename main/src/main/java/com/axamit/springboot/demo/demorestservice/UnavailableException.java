package com.axamit.springboot.demo.demorestservice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Note: for conventional API exceptions it is better to use
 * ControllerAdvice + ExceptionHandler
 * See https://www.javadevjournal.com/spring/exception-handling-for-rest-with-spring/
 */
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class UnavailableException extends RuntimeException {

    public UnavailableException(String message) {
        super(message);
    }
}
