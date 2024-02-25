package com.example.StudentSystemSpring.Controllers;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String showLoginPage(HttpSession session,@RequestParam(value = "role",required = false) String role) {
        if (session.getAttribute("user_id") != null) {
            if (role.equals("ADMIN"))
            return "redirect:/admin_dashboard";
            else if (role.equals("INSTRUCTOR"))
                return "redirect:/instructor_dashboard";
            else return "redirect:/student_dashboard";
        }
        return "login";
    }
}