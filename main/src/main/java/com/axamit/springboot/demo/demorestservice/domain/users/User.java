package com.axamit.springboot.demo.demorestservice.domain.users;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("users")
public class User {

    @Id
    @Column("user_id")
    private final UUID id;
    @Column("first_name")
    private String firstName;
    @Column("last_name")
    private String lastName;
    @Column
    private String email;
    @Column
    private String phone;

    public User() {
        this.id = null;
    }

    public User(UUID id, String firstName, String lastName, String email, String phone) {
       this.id = id;
       this.firstName = firstName;
       this.lastName = lastName;
       this.email = email;
       this.phone = phone;
    }

    public User withId(UUID id) {
        return new User(id, this.firstName, this.lastName, email, phone);
    }

    public String getId() {
        return (id != null) ? id.toString() : "";
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    void setLastName(String lastName) {
        this.lastName = lastName;
    }

    void setEmail(String email) {
        this.email = email;
    }

    void setPhone(String phone) {
        this.phone = phone;
    }
}
