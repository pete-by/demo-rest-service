package com.axamit.springboot.demo.demorestservice.controllers;

import com.axamit.springboot.demo.demorestservice.domain.users.UserDTO;
import com.axamit.springboot.demo.demorestservice.domain.users.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Inject
    private UserService userService;

    UserController() {
    }

    public UserService getUserService() {
        return userService;
    }

    @GetMapping("/users")
    List<UserDTO> users() {
        return getUserService().getUsers( PageRequest.of(0, 10) )
                .toList().stream().map(UserDTO::of).collect(Collectors.toList());
    }

    @GetMapping("/users/{id}")
    UserDTO users(@PathVariable String id) {
        Optional<UserDTO> user = getUserService().find(id).map(UserDTO::of);
        return user.orElse(null);
    }

}
