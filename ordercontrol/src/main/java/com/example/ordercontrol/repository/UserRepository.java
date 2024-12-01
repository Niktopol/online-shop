package com.example.ordercontrol.repository;

import com.example.ordercontrol.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
