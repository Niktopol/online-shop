package com.example.ordercontrol.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "order_goods")
public class OrderGood {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "good_id", nullable = false)
    private Good goodType;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer amount;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    public OrderGood(Good goodType, Double price, Integer amount, Order order){
        this.goodType = goodType;
        this.price = price;
        this.amount = amount;
        this.order = order;
    }
}
