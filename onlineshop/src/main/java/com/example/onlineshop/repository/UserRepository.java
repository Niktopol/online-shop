package com.example.onlineshop.repository;

import com.example.onlineshop.model.entity.User;
import com.example.onlineshop.model.entity.User.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
