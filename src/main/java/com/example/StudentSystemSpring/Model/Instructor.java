package com.example.StudentSystemSpring.Model;

import com.example.StudentSystemSpring.Data.DAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Instructor extends User {
    DAO dao = new DAO();
    public Instructor(int id, String password) {
        super(id, password, Role.INSTRUCTOR);
    }
    @Override
    public boolean isValidUser() {
        try(Connection connection = dao.getDatabaseConnection()) {
            System.out.println("The Instructor had Successfully Connected for the Database.");
            String query = "SELECT instructor_id FROM instructors WHERE instructor_id = ? AND password = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database: " + e.getMessage());
            return false;
        }
    }
}
