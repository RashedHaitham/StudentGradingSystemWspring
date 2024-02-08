package com.example.StudentSystemSpring.Controllers;

import com.example.StudentSystemSpring.Data.DAO;
import com.example.StudentSystemSpring.Model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class ViewCourseController {

    private final DAO dao;

    @Autowired
    public ViewCourseController(DAO dao) {
        this.dao = dao;
    }

    @GetMapping("/viewCourses")
    public String viewCourses(@RequestParam("user_id") String userIdStr,
                              @RequestParam("role") String roleStr,
                              Model model) {
        int userId = Integer.parseInt(userIdStr);
        Role role = Role.valueOf(roleStr);
        model.addAttribute("username", dao.getDbUsername(role, userId));
        Map<Integer, String> courses = dao.viewCourses(userId, role);
        model.addAttribute("courses", courses);
        if (role == Role.INSTRUCTOR) {
            Map<Integer, Integer> studentsCount = dao.getStudentCountForCourses();
            model.addAttribute("studentsCount", studentsCount);
        }
        return "view_course";
    }
}
