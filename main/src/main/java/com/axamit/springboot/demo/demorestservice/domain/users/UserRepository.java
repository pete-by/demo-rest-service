package com.axamit.springboot.demo.demorestservice.domain.users;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends PagingAndSortingRepository<User, String> {

    List<User> findByFirstName(String firstName);

    List<User> findByFirstNameOrderByLastName(String firstName, Pageable pageable);

    User findByFirstNameAndLastName(String firstName, String lastName);

    User findFirstByLastName(String lastName);

    @Query("SELECT * FROM users WHERE last_name = :lastName")
    List<User> findByLastName(String lastName);

}