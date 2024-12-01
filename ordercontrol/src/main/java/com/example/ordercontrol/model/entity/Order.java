package com.example.ordercontrol.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(nullable = false)
    private Integer status;

    @Column(nullable = false)
    private Double price;

    @OneToMany(mappedBy = "order")
    private List<OrderGood> goods;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Order(Double price, User user){
        this.status = 0;
        this.price = price;
        this.user = user;
    }
}
