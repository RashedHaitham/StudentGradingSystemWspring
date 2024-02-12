package com.example.StudentSystemSpring.Service;

import com.example.StudentSystemSpring.Entity.securityUserEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface UserSecurityService {
    void saveUser(securityUserEntity user);
    void deleteById(String idToDelete);
    void updateUser(String id,String newId);

    Optional<securityUserEntity> findById(String id);
}
