package com.example.StudentSystemSpring.Service;

import com.example.StudentSystemSpring.Entity.securityUserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserSecurityService userSecurityService;
    @Autowired
    public CustomUserDetailsService(UserSecurityService userSecurityService) {
        this.userSecurityService = userSecurityService;
    }

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        Optional<securityUserEntity> user= userSecurityService.findById(id);
        if (user.isEmpty()){
            throw new UsernameNotFoundException("user not found");
        }
        return new org.springframework.security.core.userdetails.User(
                user.get().getId(),
                user.get().getPassword(),
                AuthorityUtils.createAuthorityList(user.get().getRole()));
    }
}
