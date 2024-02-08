package com.example.StudentSystemSpring.Controllers;

import com.example.StudentSystemSpring.Data.DAO;
import com.example.StudentSystemSpring.Model.Role;
import com.example.StudentSystemSpring.Model.StudentGrades;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;;import java.util.Map;

@Controller
public class StudentController {

    private final DAO dao;

    @Autowired
    public StudentController(DAO dao){
        this.dao = dao;
    }

    @GetMapping("/studentDashboard")
    public String showStudentsDashboard(){
        return "student_dashboard";
    }

    @GetMapping("/view")
    public String viewGrades(@RequestParam("user_id") String userIdStr,
                             @RequestParam("role") String roleStr,
                             Model model){
        int userId = Integer.parseInt(userIdStr);
        Role role = Role.valueOf(roleStr);
        StudentGrades studentGrades = dao.viewGrades(userId);
        model.addAttribute("username", dao.getDbUsername(role, userId));
        model.addAttribute("studentGrades", studentGrades);
        Map<Integer, String> courses = dao.viewCourses(userId, role);
        System.out.println(courses);
        model.addAttribute("courses", courses);

        return "view_course_student";
    }
}
