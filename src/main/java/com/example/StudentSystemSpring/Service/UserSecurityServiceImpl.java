package com.example.StudentSystemSpring.Service;

import com.example.StudentSystemSpring.Entity.securityUserEntity;
import com.example.StudentSystemSpring.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserSecurityServiceImpl implements UserSecurityService{

    UserRepository repository;

    @Autowired
    public UserSecurityServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public void saveUser(securityUserEntity user) {
        repository.save(user);
    }

    @Override
    public void deleteById(String idToDelete) {
        repository.deleteById(idToDelete);
    }

    @Override
    public void updateUser(String id,String newId){
        repository.updateById(id,newId);
    }

    @Override
    public Optional<securityUserEntity> findById(String id) {
        return repository.findById(id);
    }
}
