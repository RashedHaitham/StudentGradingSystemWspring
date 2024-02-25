package com.example.StudentSystemSpring.Controllers;

import com.example.StudentSystemSpring.Data.DAO;
import com.example.StudentSystemSpring.Model.Role;
import com.example.StudentSystemSpring.Model.StudentGrades;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;;import java.util.Map;

@Controller
@RequestMapping("student")
public class StudentController {

    private final DAO dao;

    @GetMapping("/student_dashboard")
    public String dashboard(@RequestParam("username") String username,
                            @RequestParam("user_id") String user_id,
                            Model model){
        model.addAttribute("username",username);
        model.addAttribute("user_id",user_id);
        return "student_dashboard";
    }
    @Autowired
    public StudentController(DAO dao){
        this.dao = dao;
    }

    @GetMapping("/view")
    public String viewGrades(@RequestParam("user_id") String userIdStr,
                             @RequestParam("role") String roleStr,
                             Model model){
        model.addAttribute("user_id",userIdStr);
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

    @PostMapping("/dropCourse")
    public String dropCourse(@RequestParam("user_id") String userId,
                             @RequestParam("courseId") String courseId,
                             @RequestParam("role") String role,
                             RedirectAttributes redirectAttributes) {
        dao.dropEnrolledCourse(userId, courseId, role);
        redirectAttributes.addFlashAttribute("message", "Successfully Dropped the course!");
        redirectAttributes.addAttribute("user_id", userId); // These will be added as query parameters
        redirectAttributes.addAttribute("role", "STUDENT");
        return "redirect:/student/view";
    }
}
