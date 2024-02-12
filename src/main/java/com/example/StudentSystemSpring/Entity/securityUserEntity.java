package com.example.StudentSystemSpring.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name = "users")
public class securityUserEntity {
    @Id
    @Column(name = "user_id")
   private String id;

    @Column(name = "password")
    private String password;

    @Column(name = "user_type")
    private String role;

    public securityUserEntity(String id, String password, String role) {
        this.id=id;
        this.password=password;
        this.role=role;
    }

    public securityUserEntity() {
    }
}
