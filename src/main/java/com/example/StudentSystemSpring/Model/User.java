package com.example.StudentSystemSpring.Model;


import lombok.*;

@Setter
@Getter
@NoArgsConstructor
public abstract class User {
    protected int id;
    protected String username;
    protected String password;
    protected Role role;

    public User(int id, String password, Role role) {
        this.id = id;
        this.password = password;
        this.role = role;
    }

    public abstract boolean isValidUser();
}