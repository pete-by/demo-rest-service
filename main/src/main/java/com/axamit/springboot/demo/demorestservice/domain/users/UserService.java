package com.axamit.springboot.demo.demorestservice.domain.users;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface UserService {

    Optional<User> find(String id);

    Page<User> getUsers(Pageable pageable);

}
