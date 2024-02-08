package com.example.StudentSystemSpring.Util;

import com.example.StudentSystemSpring.Model.Role;
import com.example.StudentSystemSpring.Model.User;
import com.example.StudentSystemSpring.Model.UserFactory;

public class Validation {

    private Validation() {}
    public static String validateUserInput(Role role, String userIdStr, String password) {
        User user;
        if (!userIdStr.matches("\\d{7}")) {
            return "Please enter a valid 7-digit numeric " + role.toString().toLowerCase() + " ID.";
        } else {
            int userId = Integer.parseInt(userIdStr);
            password = PasswordHashing.hashPassword(password);
            user = new UserFactory().createUser(role, userId, password);
        }
        if (user.isValidUser()) {
            return null;
        } else {
            return "Invalid " + role.toString().toLowerCase() + " Credentials!!";
        }
    }
}