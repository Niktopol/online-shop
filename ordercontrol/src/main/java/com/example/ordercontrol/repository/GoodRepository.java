package com.example.ordercontrol.repository;

import com.example.ordercontrol.model.entity.Good;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoodRepository extends JpaRepository<Good, Long> {
}
