package com.example.goodscontrol.model.entity;

import com.example.goodscontrol.model.entity.key.UserGoodKey;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "cart_goods")
public class Cart {
    @EmbeddedId
    UserGoodKey id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne
    @MapsId("goodId")
    @JoinColumn(name = "good_id")
    Good good;

    @Column(nullable = false)
    Integer count;
}
