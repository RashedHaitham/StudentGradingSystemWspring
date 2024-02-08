package com.example.StudentSystemSpring.Controllers;

import com.example.StudentSystemSpring.Data.DAO;
import com.example.StudentSystemSpring.Model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.StudentSystemSpring.Model.StudentGrades;

import java.util.List;
import java.util.Map;

@Controller
public class InstructorController {
    private final DAO dao;

    @Autowired
    public InstructorController(DAO dao) {
        this.dao = dao;
    }

    @GetMapping("/manage")
    public String showCourses(@RequestParam("user_id") String user_id,
                              @RequestParam("role") String Therole,
                              Model model) {
        int userId = Integer.parseInt(user_id);
        Role role = Role.valueOf(Therole);

        model.addAttribute("username",dao.getDbUsername(role,userId));
        model.addAttribute("user_id",userId);

        Map<Integer, String> courses = dao.viewCourses(userId, role);
        model.addAttribute("courses",courses);
        // if it is instructor get the student count for each course
        if (role == Role.INSTRUCTOR) {
            Map<Integer, Integer> studentsCount = dao.getStudentCountForCourses();
            model.addAttribute("studentsCount", studentsCount);
            return "view_course";
        }
        else {
            StudentGrades studentGrades = dao.viewGrades(userId);
            model.addAttribute("username", dao.getDbUsername(role, userId));
            model.addAttribute("studentGrades", studentGrades);
            return "view_courses_student";
        }
    }

    @GetMapping("/gradeStudent")
    public String showGradeStudentPage(
            @RequestParam("user_id") String userIdStr,
            @RequestParam("courseName") String courseName,
            @RequestParam(value = "course_id", defaultValue = "-1") int courseId,
            @RequestParam("role") String roleString,
            Model model) {

        Role role = Role.valueOf(roleString);
        int userId = Integer.parseInt(userIdStr);
        model.addAttribute("courseName", courseName);
        model.addAttribute("username", dao.getDbUsername(role, userId));
        model.addAttribute("courses", dao.viewCourses(userId, role));
        model.addAttribute("stds", dao.getAllStudentsForCourse(courseId));
        model.addAttribute("role", role);
        model.addAttribute("user_id", userId);
        model.addAttribute("course_id", courseId);
        return "grade_student";
    }

    @PostMapping("/gradeStudent")
    public String gradeStudent(
            @RequestParam("user_id") String userIdStr,
            @RequestParam("course_id") int courseId,
            @RequestParam("student_id") int studentId,
            @RequestParam("grade") float grade,
            @RequestParam("courseName") String courseName,
            @RequestParam("role") String role,
            RedirectAttributes redirectAttributes) {

        int userId = Integer.parseInt(userIdStr);
        boolean gradeExists = dao.gradeExistsForStudent(studentId, courseId);
        boolean success = false;
        String successMessage="";
        if (!gradeExists) { //add
            success = dao.addOrUpdateStudentGrade(courseId, studentId, grade, "Add");
            successMessage = success ? "Grade added successfully!" : "Error adding grade. Please try again.";
        } else {
            success = dao.addOrUpdateStudentGrade(courseId, studentId, grade, "Update");
            successMessage = success ? "Grade updated successfully!" : "Error updating grade. Please try again.";
        }
        redirectAttributes.addAttribute("courseName",courseName);
        redirectAttributes.addFlashAttribute("successMessage", successMessage);
        redirectAttributes.addAttribute("user_id", userId);
        redirectAttributes.addAttribute("role", role);
        redirectAttributes.addAttribute("course_id", courseId);
        return "redirect:/gradeStudent";
    }

    @GetMapping("/gradeAnalysis")
    public String showGradeAnalysisPage(
            @RequestParam("user_id") String userIdStr,
            @RequestParam("role") String roleString,
            @RequestParam("courseName") String CourseName,
            @RequestParam("course_id") String course_id,
            Model model) {

        Role role = Role.valueOf(roleString);
        int userId = Integer.parseInt(userIdStr);

        List<Float> grades = dao.getGradesByCourse(Integer.parseInt(course_id));
        double average = dao.getAverage(grades);
        float median = dao.getMedian(grades);
        float highest = dao.getHighestGrade(grades);
        float lowest = dao.getLowestGrade(grades);

        model.addAttribute("average", average);
        model.addAttribute("median", median);
        model.addAttribute("highest", highest);
        model.addAttribute("lowest", lowest);
        model.addAttribute("courseName", CourseName);
        model.addAttribute("role", role);
        model.addAttribute("user_id", userId);

        return "grades_analysis";
    }
}
