package com.example.StudentSystemSpring.Model;

import com.example.StudentSystemSpring.Data.DAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Student extends User {
    DAO dao = new DAO();
    public Student(int id, String password) {
        super(id, password, Role.STUDENT);
    }
    @Override
    public boolean isValidUser() {
        try (Connection connection = dao.getDatabaseConnection()) {
            String query = "SELECT student_id FROM students WHERE student_id = ? AND password = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(!resultSet.next()) {
                System.out.println("The student entered wrong credentials");
                return false;
            }
            System.out.println("The Student had Successfully Connected");
            return true;
        } catch (Exception e) {
            System.err.println("Error with the database operation: " + e.getMessage());
            return false;
        }
    }
}
