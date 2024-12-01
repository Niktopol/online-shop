package com.example.ordercontrol.repository;

import com.example.ordercontrol.model.entity.OrderGood;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderGoodRepository extends JpaRepository<OrderGood, Long> {
}
