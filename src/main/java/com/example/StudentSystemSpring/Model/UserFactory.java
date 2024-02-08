package com.example.StudentSystemSpring.Model;

public class UserFactory {
    public User createUser(Role role, int id, String password) {
        return switch (role) {
            case ADMIN -> new Admin(id, password);
            case STUDENT -> new Student(id, password);
            case INSTRUCTOR -> new Instructor(id, password);
        };
    }
}
