package com.axamit.springboot.demo.demorestservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class DemoRestServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoRestServiceApplication.class, args);
	}

}

@RestController
class HelloController {

	private final String greeting;

	HelloController(@Value("${greeting:Hi}") String greeting) {
		this.greeting = greeting;
	}

	@GetMapping("/hello")
	String hello(String name) {
		return greeting + ", " + name;
	}

}
