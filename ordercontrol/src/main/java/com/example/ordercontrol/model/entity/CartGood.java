package com.example.ordercontrol.model.entity;

import com.example.ordercontrol.model.entity.key.UserGoodKey;
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
public class CartGood {
    @EmbeddedId
    UserGoodKey id;

    @Version
    private Long version;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne
    @MapsId("goodId")
    @JoinColumn(name = "good_id")
    Good good;

    @Column(nullable = false)
    Integer amount;

    public CartGood(User user, Good good){
        this.amount = 1;
        this.user = user;
        this.good = good;
        this.id = new UserGoodKey(user.getId(), good.getId());
    }
}
