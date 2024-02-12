package com.example.StudentSystemSpring.Controllers;

import com.example.StudentSystemSpring.Data.DAO;
import com.example.StudentSystemSpring.Model.Role;
import com.example.StudentSystemSpring.Util.Validation;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    //private final DAO dao;

//    @Autowired
//    public LoginController(DAO dao) {
//        this.dao = dao;
//    }

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

//    @PostMapping("/login")
//    public String login(@RequestParam("username") String userIdStr,
//                        @RequestParam("password") String password,
//                        @RequestParam(value = "role", required = false) String roleStr,
//                        HttpSession session,
//                        Model model) {
//        Role role;
//            if (dao.isAdminCredentials(userIdStr, password)) {
//                role = Role.ADMIN;
//            }
//        else {
//            role = Role.valueOf(roleStr);
//            String errorMessage = Validation.validateUserInput(role, userIdStr, password);
//            if (errorMessage != null) {
//                model.addAttribute("errorMessage", errorMessage);
//                return "login";
//            }
//        }
//        session.setAttribute("user_id",userIdStr);
//        if (role == Role.STUDENT || role == Role.INSTRUCTOR) {
//            int userId = Integer.parseInt(userIdStr);
//            model.addAttribute("user_id", userId);
//            model.addAttribute("username", dao.getDbUsername(role, userId));
//            return role == Role.STUDENT ? "student_dashboard" : "instructor_dashboard";
//        } else if (role == Role.ADMIN) {
//            model.addAttribute("username", userIdStr);
//            return "redirect:/admin/admin_dashboard";
//        }
//        return "login";
//    }
}