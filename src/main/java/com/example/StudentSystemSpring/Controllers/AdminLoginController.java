package com.example.StudentSystemSpring.Controllers;

import com.example.StudentSystemSpring.Data.DAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

@Controller
//@SessionAttributes("user_id")
public class AdminLoginController {
    private final DAO dao;

    @Autowired
    public AdminLoginController(DAO dao) {
        this.dao = dao;
    }

    @GetMapping("/adminLogin")
    public String showLoginPage() {
        return "adminLogin";
    }

    @PostMapping("/adminLogin")
    public String login(@RequestParam("user_id") String userIdStr,
                        @RequestParam("password") String password,
                        Model model) {
            if(dao.isAdminCredentials(userIdStr,password)) {
                model.addAttribute("username", userIdStr);
                return "admin_dashboard";
            }
            else{
                model.addAttribute("errorMessage", "Wrong credentials, please try again.");
                return "adminLogin";
            }
    }


}
