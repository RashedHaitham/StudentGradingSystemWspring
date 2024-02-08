package com.example.StudentSystemSpring.Data;

import com.example.StudentSystemSpring.Model.CourseGrade;
import com.example.StudentSystemSpring.Model.Role;
import com.example.StudentSystemSpring.Model.StudentGrades;
import org.springframework.stereotype.Service;
import java.sql.*;
import java.util.*;

@Service
public class DAO {
    private static final String HOST = "jdbc:mysql://localhost:3306/";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "2002";
    private static final String DB_DATABASE = "directory";
    private final Map<String, List<String>> tableColumns = new HashMap<>();

    public DAO() {
        tableColumns.put("students", Arrays.asList("student_id", "username"));
        tableColumns.put("instructors", Arrays.asList("instructor_id", "username"));
        tableColumns.put("courses", Arrays.asList("course_id", "course_name"));
    }

    public List<String> getTableColumns(String tableName) {
        return tableColumns.get(tableName);
    }

    public Connection getDatabaseConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(HOST + DB_DATABASE, DB_USERNAME, DB_PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver not found", e);
        }
    }

    private int executeUpdate(String query, Object... params) throws SQLException {
        try (Connection connection = getDatabaseConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }
            return preparedStatement.executeUpdate();
        }
    }

    private ResultSet executeQuery(String query, Object... params) throws SQLException {
        Connection connection = getDatabaseConnection();
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        for (int i = 0; i < params.length; i++) {
            preparedStatement.setObject(i + 1, params[i]);
        }
        return preparedStatement.executeQuery();
    }

    public List<Float> getGradesByCourse(int courseId) {
        List<Float> grades = new ArrayList<>();
        try {
            ResultSet resultSet = executeQuery("SELECT grade FROM grades WHERE course_id = ?", courseId);
            while (resultSet.next()) {
                grades.add(resultSet.getFloat("grade"));
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return grades;
    }

    public double getAverage(List<Float> grades) {
        if (grades.isEmpty()) {
            return 0.0;
        }
        double total = 0.0;
        for (float grade : grades) {
            total += grade;
        }
        return total / grades.size();
    }

    public float getMedian(List<Float> grades) {
        if (grades.isEmpty()) {
            return 0;
        }
        List<Float> sortedGrades = new ArrayList<>(grades);
        Collections.sort(sortedGrades);

        int size = sortedGrades.size();
        if (size % 2 == 1) {
            return Math.round(sortedGrades.get(size / 2));
        } else {
            float median1 = sortedGrades.get(size / 2 - 1);
            float median2 = sortedGrades.get(size / 2);
            return Math.round((median1 + median2) / 2);
        }
    }

    public float getHighestGrade(List<Float> grades) {
        if (grades.isEmpty()) {
            return 0;
        }
        float highest = Float.MIN_VALUE;
        for (float grade : grades) {
            if (grade > highest) {
                highest = grade;
            }
        }
        return highest;
    }

    public float getLowestGrade(List<Float> grades) {
        if (grades.isEmpty()) {
            return 0;
        }
        float lowest = Float.MAX_VALUE;
        for (float grade : grades) {
            if (grade < lowest) {
                lowest = grade;
            }
        }
        return lowest;
    }

    public boolean isAdminCredentials(String userIdStr, String password){
        return userIdStr.equalsIgnoreCase(DB_USERNAME) && password.equalsIgnoreCase(DB_PASSWORD);
    }


    public Map<Integer, String> viewCourses(int id, Role role) {
        Map<Integer, String> courses = new HashMap<>();
        String query = getQueryForRole(role);
        return getStringMap(id, courses, query);
    }

    private String getQueryForRole(Role role) {
        if (role.equals(Role.INSTRUCTOR)) {
            return "SELECT course_id, course_name " +
                    "FROM courses " +
                    "WHERE course_id IN (SELECT course_id FROM instructor_course WHERE instructor_id = ?)";
        } else if (role.equals(Role.STUDENT)) {
            return "SELECT course_id, course_name " +
                    "FROM courses " +
                    "WHERE course_id IN (SELECT course_id FROM student_course WHERE student_id = ?)";
        }
        return null;
    }

    public Map<Integer, String> getAvailableCourses(int userId, Role role) {
        Map<Integer, String> availableCourses = new HashMap<>();
        String query = "";
        if (role == Role.STUDENT) {
            query = "SELECT course_id, course_name FROM courses WHERE course_id NOT IN (SELECT course_id FROM student_course WHERE student_id = ?)";
        } else if (role == Role.INSTRUCTOR) {
            query = "SELECT course_id, course_name FROM courses WHERE course_id NOT IN (SELECT course_id FROM instructor_course WHERE instructor_id = ?)";
        }
        return getStringMap(userId, availableCourses, query);
    }

    private Map<Integer, String> getStringMap(int userId, Map<Integer, String> availableCourses, String query) {
        try(ResultSet rs = executeQuery(query, userId)) {
            while (rs.next()) {
                int courseId = rs.getInt("course_id");
                String courseName = rs.getString("course_name");
                availableCourses.put(courseId, courseName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return availableCourses;
    }

    public StudentGrades viewGrades(int studentIdInput) {
        List<CourseGrade> courseGrades = new ArrayList<>();
        String studentName = "";
        try {
            String query = "SELECT students.username AS student_name, courses.course_id, courses.course_name, grades.grade " +
                    "FROM students " +
                    "JOIN grades ON students.student_id = grades.student_id " +
                    "JOIN courses ON courses.course_id = grades.course_id " +
                    "WHERE students.student_id = ?";
            try (Connection connection = getDatabaseConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, studentIdInput);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        studentName = resultSet.getString("student_name");
                        int courseId = resultSet.getInt("course_id");
                        String courseName = resultSet.getString("course_name");
                        double grade = resultSet.getDouble("grade");
                        courseGrades.add(new CourseGrade(courseId, courseName, grade));
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return new StudentGrades(studentIdInput, studentName, courseGrades);
    }

    public boolean gradeExistsForStudent(int studentId, int courseId) {
        String query = "SELECT * FROM grades WHERE student_id = ? AND course_id = ?";
        try (ResultSet rs = executeQuery(query, studentId, courseId)) {
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Map<Integer, Integer> getStudentCountForCourses() {
        String query = "SELECT course_id, COUNT(student_id) AS student_count FROM student_course GROUP BY course_id";
        Map<Integer, Integer> studentCounts = new HashMap<>();
        try (ResultSet rs = executeQuery(query)) {
            while (rs.next()) {
                int courseId = rs.getInt("course_id");
                int count = rs.getInt("student_count");
                studentCounts.put(courseId, count);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return studentCounts;
    }

    public List<String[]> getTableContent(String tableName) {
        String query = "SELECT * FROM " + tableName;
        List<String[]> tableContent = new ArrayList<>();
        List<String> columns = tableColumns.get(tableName);
        try (ResultSet resultSet = executeQuery(query)) {
            while (resultSet.next()) {
                String[] row = new String[columns.size()];
                for (int i = 0; i < columns.size(); i++) {
                    row[i] = resultSet.getString(columns.get(i));
                }
                tableContent.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tableContent;
    }

    public Map<Integer, StudentGrades> getAllStudentsForCourse(int courseId) {
        String query = "SELECT s.student_id, s.username, g.grade " +
                "FROM students s " +
                "JOIN student_course sc ON s.student_id = sc.student_id " +
                "LEFT JOIN grades g ON s.student_id = g.student_id AND sc.course_id = g.course_id " +
                "WHERE sc.course_id = ?";
        Map<Integer, StudentGrades> studentsMap = new HashMap<>();
        try (ResultSet resultSet = executeQuery(query, courseId)) {
            while (resultSet.next()) {
                int studentId = resultSet.getInt("student_id");
                String studentName = resultSet.getString("username");
                double grade = resultSet.getDouble("grade");
                CourseGrade courseGrade;
                if (resultSet.wasNull()) {
                    courseGrade = new CourseGrade(courseId, "No Course", Double.NaN);
                } else {
                    courseGrade = new CourseGrade(courseId, "Unknown", grade);
                }
                List<CourseGrade> courseGrades = new ArrayList<>();
                courseGrades.add(courseGrade);
                StudentGrades studentGrades = new StudentGrades(studentId, studentName, courseGrades);
                studentsMap.put(studentId, studentGrades);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return studentsMap;
    }

    public boolean addRecord(String tableName, Map<String, String> inputData) {
        return insertRecord(tableName, inputData);
    }

    private boolean insertRecord(String tableName, Map<String, String> inputData) {
        StringJoiner columns = new StringJoiner(", ");
        StringJoiner values = new StringJoiner(", ");
        for (String field : inputData.keySet()) {
            columns.add(field);
            values.add("?");
        }
        String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ")";
        try {
            executeUpdate(sql, inputData.values().toArray());
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /*public boolean updateRecord(String tableName, String columnToUpdate,
                                String primaryKeyColumn, String idToUpdate, String newValue) {
        String query = "UPDATE " + tableName + " SET " + columnToUpdate + " = ? WHERE " + primaryKeyColumn + " = ?";
        System.out.println(tableName);
        System.out.println(columnToUpdate);
        System.out.println(primaryKeyColumn);
        System.out.println(query);
        try {
            int rowsUpdated = executeUpdate(query, newValue, idToUpdate);
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }*/

    public boolean updateRecord(String tableName, String columnToUpdate,
                                String primaryKeyColumn, String idToUpdate, String newValue) {

        if(columnToUpdate.equals("username")||columnToUpdate.equals("course_name")) {
            String query = "UPDATE " + tableName + " SET " + columnToUpdate + " = ? WHERE " + primaryKeyColumn + " = ?";
            try {
                int rowsUpdated = executeUpdate(query, newValue, idToUpdate);
                return rowsUpdated > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }

        String disableFKQuery = "SET FOREIGN_KEY_CHECKS=0;";
        String enableFKQuery = "SET FOREIGN_KEY_CHECKS=1;";
        String updateQuery = "UPDATE " + tableName + " SET " + columnToUpdate + " = ? WHERE " + primaryKeyColumn + " = ?";

// Assuming we have a method that returns a Map of related tables and their respective foreign key columns
        Map<String, String> relatedTables = getRelatedTables(tableName, primaryKeyColumn);

        Connection connection = null;
        try {
            connection = getDatabaseConnection();
            connection.setAutoCommit(false); // Disable auto-commit mode

// Disable foreign key checks
            executeUpdate(connection, disableFKQuery);

// Update the main table
            executeUpdate(connection, updateQuery, newValue, idToUpdate);

// Update all related tables
            for (Map.Entry<String, String> entry : relatedTables.entrySet()) {
                String relatedUpdateQuery = "UPDATE " + entry.getKey() + " SET " + entry.getValue() + " = ? WHERE " + entry.getValue() + " = ?";
                executeUpdate(connection, relatedUpdateQuery, newValue, idToUpdate);
            }

// Re-enable foreign key checks
            executeUpdate(connection, enableFKQuery);

// Commit the transaction
            connection.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            try {
// If there's an error, rollback any changes
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (connection != null) {
                    connection.setAutoCommit(true); // Restore auto-commit mode
                    connection.close(); // Close the connection
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void executeUpdate(Connection connection, String query, Object... params) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            for (int i = 0; i < params.length; i++) {
                preparedStatement.setObject(i + 1, params[i]);
            }
            preparedStatement.executeUpdate();
        }
    }

    //Stub for getting related tables and foreign key columns
    private Map<String, String> getRelatedTables(String tableName, String primaryKeyColumn) {
        Map<String, String> relatedTables = new HashMap<>();

        // Determine the related tables and foreign keys based on the table name
        switch (tableName) {
            case "students":
                // Assuming student_id is the primary key column in the students table
                relatedTables.put("grades", "student_id");
                relatedTables.put("student_course", "student_id");
                break;
            case "instructors":
                // Assuming instructor_id is the primary key column in the instructors table
                relatedTables.put("instructor_course", "instructor_id");
                break;
            case "courses":
                // Assuming course_id is the primary key column in the courses table
                relatedTables.put("grades", "course_id");
                relatedTables.put("student_course", "course_id");
                relatedTables.put("instructor_course", "course_id");
                break;
            // Add cases for other tables if necessary
        }

        return relatedTables;
    }

    public boolean deleteRecord(String tableName, String idToDelete) {
        boolean isDeleted = false;
        if (tableName.equals("students")) {
            isDeleted = deleteStudentGrade(idToDelete, tableName);
        }
        if (!tableName.equals("courses")) {
            isDeleted = deleteEnrolledCourse(idToDelete, tableName);
        }
        if (tableName.equals("courses")) {
            isDeleted = true;
        }

            List<String> columns = tableColumns.get(tableName);
            String primaryKeyColumn = columns.get(0);
            String deleteSQL = "DELETE FROM " + tableName + " WHERE " + primaryKeyColumn + " = ?";
            try {
                int rowsDeleted = executeUpdate(deleteSQL, idToDelete);
                return rowsDeleted > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }

        return false;
    }

    public boolean deleteStudentGrade(String idToDelete, String table) {
        String deleteQuery = null;
        if (table.equals("students")) {
            deleteQuery = "DELETE FROM grades WHERE student_id = ?";
        }
        try (Connection ignored = getDatabaseConnection()) {
            int rowsDeleted = executeUpdate(deleteQuery, idToDelete);
            return rowsDeleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteEnrolledCourse(String userId, String userType) {
        String deleteQuery = null;
        if (userType.equals("students")) {
            deleteQuery = "DELETE FROM student_course WHERE student_id = ?";
        } else if (userType.equals("instructors")) {
            deleteQuery = "DELETE FROM instructor_course WHERE instructor_id = ?";
        }
        try (Connection ignored = getDatabaseConnection()) {
            int rowsDeleted = executeUpdate(deleteQuery, userId);
            return rowsDeleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean enrollCourse(int userId, Role role, String tableName, String courseId) {
        String sql = String.format("INSERT INTO %s (%s_id, course_id) VALUES (?, ?)", tableName, role.toString().toLowerCase());
        try {
            int rowsInserted = executeUpdate(sql, userId, courseId);
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addOrUpdateStudentGrade(int courseId, int studentId, float grade, String gradeOrUpdate) {
        String query;
        try (Connection ignored = getDatabaseConnection()) {
            if (gradeOrUpdate.equals("Add")) {
                query = "INSERT INTO grades (student_id, course_id, grade) VALUES (?, ?, ?)";
                int rowsInserted = executeUpdate(query, studentId, courseId, grade);
                return rowsInserted > 0;
            } else {
                query = "UPDATE grades SET grade = ? WHERE student_id = ? AND course_id = ?";
                int rowsAffected = executeUpdate(query, grade, studentId, courseId);
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getDbUsername(Role role, int id) {
        String query = "";
        String res = "";
        if (role.equals(Role.INSTRUCTOR) || role.equals(Role.STUDENT)) {
            query = "SELECT username FROM " + (role.equals(Role.INSTRUCTOR) ? "instructors" : "students") + " WHERE " +
                    (role.equals(Role.INSTRUCTOR) ? "instructor_id" : "student_id") + " = ?";
        }
        try(ResultSet resultSet = executeQuery(query, id)){
            if (resultSet.next()) {
                res = resultSet.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return res;
    }

    public boolean isIdExists(String id, String tableName) {
        String query = checkTableAccess(tableName);
        try(ResultSet resultSet = executeQuery(query, id)){
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String checkTableAccess(String tableName) {
        return switch (tableName) {
            case "students" -> "SELECT student_id FROM " + tableName + " WHERE student_id = ?";
            case "instructors" -> "SELECT instructor_id FROM " + tableName + " WHERE instructor_id = ?";
            case "courses" -> "SELECT course_id FROM " + tableName + " WHERE course_id = ?";
            default -> throw new IllegalArgumentException();
        };
    }
}