package com.example.goodscontrol.repository;

import com.example.goodscontrol.model.entity.Good;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GoodsRepository extends JpaRepository<Good, Long> {
    List<Good> findAllByNameContainingIgnoreCase(String name);
}
