package com.example.StudentSystemSpring.Repository;

import com.example.StudentSystemSpring.Entity.securityUserEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;


public interface UserRepository extends JpaRepository<securityUserEntity, String> {

    @Transactional
    @Modifying
    @Query("UPDATE securityUserEntity s SET s.id = :newId WHERE s.id = :id")
    int updateById(String id, String newId);
}
