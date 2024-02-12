package com.example.StudentSystemSpring.Security;

import com.example.StudentSystemSpring.Data.DAO;
import com.example.StudentSystemSpring.Model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Set;

@Service
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final DAO dao;
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    @Autowired
    public CustomAuthenticationSuccessHandler(DAO dao) {
        this.dao = dao;
    }
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        String username=authentication.getName();
        if (roles.contains("ADMIN")) {
            redirectStrategy.sendRedirect(request, response, "/admin/admin_dashboard");
        } else if(roles.contains("INSTRUCTOR")){
            redirectStrategy.sendRedirect(request, response, "/instructor/instructor_dashboard?user_id="
                    +username+"&username="+dao.getDbUsername(Role.INSTRUCTOR, Integer.parseInt(username)));
        }
        else redirectStrategy.sendRedirect(request, response, "/student/student_dashboard?user_id="
                    +username+"&username="+ dao.getDbUsername(Role.STUDENT,Integer.parseInt(username)));

    }
}
