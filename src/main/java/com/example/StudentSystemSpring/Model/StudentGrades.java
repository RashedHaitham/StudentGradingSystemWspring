package com.example.StudentSystemSpring.Model;

import java.util.List;

public class StudentGrades {
    private final int studentId;
    private final String studentName;
    private final List<CourseGrade> courseGrades;

    public StudentGrades(int studentId, String studentName, List<CourseGrade> courseGrades) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.courseGrades = courseGrades;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public List<CourseGrade> getCourseGrades() {
        return courseGrades;
    }
}