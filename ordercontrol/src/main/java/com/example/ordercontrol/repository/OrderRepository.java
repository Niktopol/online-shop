package com.example.ordercontrol.repository;

import com.example.ordercontrol.model.entity.Order;
import com.example.ordercontrol.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByUser(User user);
    List<Order> findByStatusBetween(Integer a, Integer b);
}
