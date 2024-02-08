package com.example.StudentSystemSpring.Model;

public class CourseGrade {
    private int courseId;
    private String courseName;
    private double grade;

    public CourseGrade(int courseId, String courseName, double grade) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.grade = grade;
    }

    public int getCourseId(){
        return courseId;
    }

    public void setCourseId(int courseId){
        this.courseId = courseId;
    }
    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public double getGrade() {
        return grade;
    }

    public void setGrade(double grade) {
        this.grade = grade;
    }

    @Override
    public String toString() {
        return "CourseName: " + courseName + ", Grade: " + grade;
    }

}