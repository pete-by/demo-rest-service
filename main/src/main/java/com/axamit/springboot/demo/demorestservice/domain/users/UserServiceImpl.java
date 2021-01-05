package com.axamit.springboot.demo.demorestservice.domain.users;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Inject
    private UserRepository repository;

    public UserRepository getRepository() {
        return repository;
    }

    @Override
    public Optional<User> find(String id) {
        return getRepository().findById(id);
    }

    @Override
    public Page<User> getUsers(Pageable pageable) {
        return getRepository().findAll(pageable);
    }

}
