package com.example.StudentSystemSpring.Data;

import com.example.StudentSystemSpring.Model.CourseGrade;
import com.example.StudentSystemSpring.Model.Role;
import com.example.StudentSystemSpring.Model.StudentGrades;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class DAO {

    private final Map<String, List<String>> tableColumns = new HashMap<>();

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        initializeTableColumns();
    }

    private void initializeTableColumns() {
        tableColumns.put("students", Arrays.asList("student_id", "username"));
        tableColumns.put("instructors", Arrays.asList("instructor_id", "username"));
        tableColumns.put("courses", Arrays.asList("course_id", "course_name"));
    }

    public List<String> getTableColumns(String tableName) {
        return tableColumns.get(tableName);
    }

    public List<Float> getGradesByCourse(int courseId) {
        String sql = "SELECT grade FROM grades WHERE course_id = ?";
        return jdbcTemplate.query(sql, new Object[]{courseId}, (rs, rowNum) -> rs.getFloat("grade"));
    }

    public double getAverage(List<Float> grades) {
        if (grades.isEmpty()) return 0.0;
        return grades.stream().mapToDouble(Float::doubleValue).average().orElse(0.0);
    }

    public float getMedian(List<Float> grades) {
        if (grades.isEmpty()) return 0;
        Collections.sort(grades);
        int middle = grades.size() / 2;
        if (grades.size() % 2 == 1) {
            return grades.get(middle);
        } else {
            return (grades.get(middle - 1) + grades.get(middle)) / 2.0f;
        }
    }
    public float getHighestGrade(List<Float> grades) {
        return grades.stream().max(Float::compare).orElse(0.0f);
    }

    public float getLowestGrade(List<Float> grades) {
        return grades.stream().min(Float::compare).orElse(0.0f);
    }

    public Map<Integer, String> viewCourses(int id, Role role) {
        String sql = getQueryForRole(role);
        return jdbcTemplate.query(sql, new Object[]{id}, (rs, rowNum) -> new AbstractMap.SimpleEntry<>(rs.getInt("course_id"), rs.getString("course_name")))
                .stream().collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    private String getQueryForRole(Role role) {
        if (role.equals(Role.INSTRUCTOR)) {
            return "SELECT course_id, course_name FROM courses WHERE course_id IN (SELECT course_id FROM instructor_course WHERE instructor_id = ?)";
        } else if (role.equals(Role.STUDENT)) {
            return "SELECT course_id, course_name FROM courses WHERE course_id IN (SELECT course_id FROM student_course WHERE student_id = ?)";
        }
        return null; // Or throw an exception
    }

    public Map<Integer, String> getAvailableCourses(int userId, Role role) {
        String sql;
        if (role == Role.STUDENT) {
            sql = "SELECT course_id, course_name FROM courses WHERE course_id NOT IN (SELECT course_id FROM student_course WHERE student_id = ?)";
        } else { // Role.INSTRUCTOR
            sql = "SELECT course_id, course_name FROM courses WHERE course_id NOT IN (SELECT course_id FROM instructor_course WHERE instructor_id = ?)";
        }
        return jdbcTemplate.query(sql, new Object[]{userId}, (rs, rowNum) -> new AbstractMap.SimpleEntry<>(rs.getInt("course_id"), rs.getString("course_name")))
                .stream()
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    public StudentGrades viewGrades(int studentIdInput) {
        String sql = "SELECT students.username AS student_name, courses.course_id, courses.course_name, grades.grade " +
                "FROM students " +
                "JOIN grades ON students.student_id = grades.student_id " +
                "JOIN courses ON courses.course_id = grades.course_id " +
                "WHERE students.student_id = ?";

        List<CourseGrade> courseGrades = new ArrayList<>();
        String[] studentName = new String[1]; // Use an array to hold the student name, hack to overcome lambda final variable restriction

        jdbcTemplate.query(sql, new Object[]{studentIdInput}, rs -> {
            if (studentName[0] == null) { // Set the student's name from the first row
                studentName[0] = rs.getString("student_name");
            }
            int courseId = rs.getInt("course_id");
            String courseName = rs.getString("course_name");
            double grade = rs.getDouble("grade");
            courseGrades.add(new CourseGrade(courseId, courseName, grade));
        });

        return new StudentGrades(studentIdInput, studentName[0], courseGrades);
    }

    public boolean gradeExistsForStudent(int studentId, int courseId) {
        String sql = "SELECT COUNT(*) FROM grades WHERE student_id = ? AND course_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{studentId, courseId}, Integer.class);
        return count != null && count > 0;
    }

    public Map<Integer, Integer> getStudentCountForCourses() {
        String sql = "SELECT course_id, COUNT(student_id) AS student_count FROM student_course GROUP BY course_id";

        return jdbcTemplate.query(sql, (ResultSet rs) -> {
            Map<Integer, Integer> studentCounts = new HashMap<>();
            while (rs.next()) {
                int courseId = rs.getInt("course_id");
                int count = rs.getInt("student_count");
                studentCounts.put(courseId, count);
            }
            return studentCounts;
        });
    }

    public List<String[]> getTableContent(String tableName) {
        List<String> columns = getTableColumns(tableName);

        String sql = "SELECT * FROM " + tableName;

        return jdbcTemplate.query(sql, (ResultSet rs) -> {
            List<String[]> tableContent = new ArrayList<>();
            while (rs.next()) {
                String[] row = new String[columns.size()];
                for (int i = 0; i < columns.size(); i++) {
                    // Safely retrieve each column by name, assuming column names are correct
                    row[i] = rs.getString(columns.get(i));
                }
                tableContent.add(row);
            }
            return tableContent;
        });
    }

    public Map<Integer, StudentGrades> getAllStudentsForCourse(int courseId) {
        String sql = "SELECT s.student_id, s.username, g.grade " +
                "FROM students s " +
                "JOIN student_course sc ON s.student_id = sc.student_id " +
                "LEFT JOIN grades g ON s.student_id = g.student_id AND sc.course_id = g.course_id " +
                "WHERE sc.course_id = ?";

        // Use RowMapper to map each row to a StudentGrades object
        List<StudentGrades> studentGradesList = jdbcTemplate.query(sql, new Object[]{courseId}, new RowMapper<StudentGrades>() {
            @Override
            public StudentGrades mapRow(ResultSet rs, int rowNum) throws SQLException {
                int studentId = rs.getInt("student_id");
                String studentName = rs.getString("username");
                Double grade = rs.getObject("grade") != null ? rs.getDouble("grade") : Double.NaN;
                CourseGrade courseGrade = new CourseGrade(courseId, "Unknown", grade);
                return new StudentGrades(studentId, studentName, List.of(courseGrade));
            }
        });

        // Convert List<StudentGrades> to Map<Integer, StudentGrades> where key is studentId
        return studentGradesList.stream().collect(Collectors.toMap(StudentGrades::getStudentId, sg -> sg, (sg1, sg2) -> {
            // In case of duplicate student IDs (which shouldn't happen in this context), merge their grades
            sg1.getCourseGrades().addAll(sg2.getCourseGrades());
            return sg1;
        }));
    }

    private Map<String, String> getRelatedTables(String tableName) {
        Map<String, String> relatedTables = new HashMap<>();

        switch (tableName) {
            case "students":
                relatedTables.put("grades", "student_id");
                relatedTables.put("student_course", "student_id");
                break;
            case "instructors":
                relatedTables.put("instructor_course", "instructor_id");
                break;
            case "courses":
                relatedTables.put("grades", "course_id");
                relatedTables.put("student_course", "course_id");
                relatedTables.put("instructor_course", "course_id");
                break;
        }

        return relatedTables;
    }

    public boolean deleteRecord(String tableName, String idToDelete) {
        List<String> columns = tableColumns.get(tableName);
        String primaryKeyColumn = columns.get(0);
        String sql = String.format("DELETE FROM %s WHERE %s = ?", tableName, primaryKeyColumn);
        return jdbcTemplate.update(sql, idToDelete) > 0;
    }

    public boolean deleteGrade(String studentId, String courseId) {
        String sql = "DELETE FROM grades WHERE student_id = ? AND course_id = ?";
        System.out.println(studentId + " " + courseId);

        int rowsDeleted = jdbcTemplate.update(sql, studentId, courseId);
        return rowsDeleted > 0;
    }

    public boolean dropEnrolledCourse(String userId, String courseId, String userRole) {
        String deleteQuery = "";
        if ("STUDENT".equals(userRole)) {
            deleteQuery = "DELETE FROM student_course WHERE student_id = ? AND course_id = ?";
        } else if ("INSTRUCTOR".equals(userRole)) {
            deleteQuery = "DELETE FROM instructor_course WHERE instructor_id = ? AND course_id = ?";
        }

        int rowsDeleted = jdbcTemplate.update(deleteQuery, userId, courseId);
        // If no rows are deleted, this might indicate the enrollment doesn't exist, hence return false.
        return rowsDeleted > 0;
    }

    public boolean enrollCourse(int userId, Role role, String tableName, String courseId) {
        String sql = String.format("INSERT INTO %s (%s_id, course_id) VALUES (?, ?)", tableName, role.toString().toLowerCase());
        return jdbcTemplate.update(sql, userId, courseId) > 0;
    }

    public boolean addOrUpdateStudentGrade(int courseId, int studentId, float grade, String gradeOrUpdate) {
        if ("Add".equals(gradeOrUpdate)) {
            String sql = "INSERT INTO grades (student_id, course_id, grade) VALUES (?, ?, ?)";
            return jdbcTemplate.update(sql, studentId, courseId, grade) > 0;
        } else {
            String sql = "UPDATE grades SET grade = ? WHERE student_id = ? AND course_id = ?";
            return jdbcTemplate.update(sql, grade, studentId, courseId) > 0;
        }
    }

    public String getDbUsername(Role role, int id) {
        String tableName = role == Role.INSTRUCTOR ? "instructors" : "students";
        String idColumn = role == Role.INSTRUCTOR ? "instructor_id" : "student_id";
        String sql = String.format("SELECT username FROM %s WHERE %s = ?", tableName, idColumn);

        return jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> rs.getString("username"));
    }

    public boolean isIdExists(String id, String tableName) {
        String sql = checkTableAccess(tableName);
        try {
            Integer count = jdbcTemplate.queryForObject(sql, new Object[]{id}, Integer.class);
            return count != null && count > 0;
        } catch (EmptyResultDataAccessException e) {
            return false;
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    private String checkTableAccess(String tableName) {
        return switch (tableName) {
            case "students" -> "SELECT COUNT(*) FROM " + tableName + " WHERE student_id = ?";
            case "instructors" -> "SELECT COUNT(*) FROM " + tableName + " WHERE instructor_id = ?";
            case "courses" -> "SELECT COUNT(*) FROM " + tableName + " WHERE course_id = ?";
            default -> throw new IllegalArgumentException("Unsupported table: " + tableName);
        };
    }

    public boolean addRecord(String tableName, Map<String, String> inputData) {
        return insertRecord(tableName, inputData);
    }

    private boolean insertRecord(String tableName, Map<String, String> inputData) {
        StringJoiner columns = new StringJoiner(", ");
        StringJoiner placeholders = new StringJoiner(", ");
        inputData.keySet().forEach(key -> {
            columns.add(key);
            placeholders.add("?");
        });
        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, placeholders);

        Object[] params = inputData.values().toArray(new Object[0]);
        int rowsAffected = jdbcTemplate.update(sql, params);
        return rowsAffected > 0;
    }


    @Transactional
    public boolean updateRecord(String tableName, String columnToUpdate,
                                String primaryKeyColumn, String idToUpdate, String newValue) {
        try {
            if ("username".equals(columnToUpdate) || "course_name".equals(columnToUpdate)) {
                String sql = String.format("UPDATE %s SET %s = ? WHERE %s = ?", tableName, columnToUpdate, primaryKeyColumn);
                int rowsUpdated = jdbcTemplate.update(sql, newValue, idToUpdate);
                return rowsUpdated > 0;
            }
            disableForeignKeyChecks();
            updateMainTable(tableName, columnToUpdate, primaryKeyColumn, idToUpdate, newValue);
            updateRelatedTables(getRelatedTables(tableName), columnToUpdate, idToUpdate, newValue);
            enableForeignKeyChecks();

            return true;
        } catch (DataAccessException e) {
            throw e;
        }
    }

    private void disableForeignKeyChecks() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0;");
    }

    private void enableForeignKeyChecks() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1;");
    }

    private void updateMainTable(String tableName, String columnToUpdate, String primaryKeyColumn,
                                 String idToUpdate, String newValue) {
        String sql = String.format("UPDATE %s SET %s = ? WHERE %s = ?", tableName, columnToUpdate, primaryKeyColumn);
        jdbcTemplate.update(sql, newValue, idToUpdate);
    }

    private void updateRelatedTables(Map<String, String> relatedTables, String columnToUpdate,
                                     String idToUpdate, String newValue) {
        relatedTables.forEach((relatedTableName, foreignKeyColumn) -> {
            String sql = String.format("UPDATE %s SET %s = ? WHERE %s = ?", relatedTableName, columnToUpdate, foreignKeyColumn);
            jdbcTemplate.update(sql, newValue, idToUpdate);
        });
    }
}