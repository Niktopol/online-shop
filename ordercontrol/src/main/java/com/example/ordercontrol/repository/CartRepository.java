package com.example.ordercontrol.repository;

import com.example.ordercontrol.model.entity.CartGood;
import com.example.ordercontrol.model.entity.Good;
import com.example.ordercontrol.model.entity.User;
import com.example.ordercontrol.model.entity.key.UserGoodKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<CartGood, UserGoodKey> {
    List<CartGood> findAllByUser(User user);
    Optional<CartGood> findByUserAndGood(User user, Good good);
}
