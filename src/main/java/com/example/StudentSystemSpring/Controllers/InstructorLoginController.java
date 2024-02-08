package com.example.StudentSystemSpring.Controllers;

import com.example.StudentSystemSpring.Data.DAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import com.example.StudentSystemSpring.Model.Role;
import com.example.StudentSystemSpring.Util.Validation;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

@Controller
//@SessionAttributes("user_id")
public class InstructorLoginController {
    private final DAO dao;

    @Autowired
    public InstructorLoginController(DAO dao) {
        this.dao = dao;
    }

    @GetMapping("/instructorLogin")
    public String showLoginPage() {
        return "instructorLogin";
    }

    @PostMapping("/instructorLogin")
    public String login(@RequestParam("user_id") String userIdStr,
                        @RequestParam("password") String password,
                        Model model) {

            String errorMessage = Validation.validateUserInput(Role.INSTRUCTOR, userIdStr, password);
            if (errorMessage != null) {
                model.addAttribute("errorMessage", errorMessage);
                return "instructorLogin";
            }

            int userId = Integer.parseInt(userIdStr);
            model.addAttribute("user_id", userId);
            model.addAttribute("username", dao.getDbUsername(Role.INSTRUCTOR, userId));
            return "instructor_dashboard";

    }

}
