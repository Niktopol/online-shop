package com.example.onlineshop.controller;

import com.example.onlineshop.exception.EmptyListException;
import com.example.onlineshop.model.dto.*;
import com.example.onlineshop.service.OrdersService;
import lombok.AllArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
@CrossOrigin
@Controller
public class OrdersController {
    OrdersService service;

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @QueryMapping
    public List<OrderDTO> getOrders(){
        return service.getOrders().join();
    }

    @PreAuthorize("hasAuthority('OWNER')")
    @QueryMapping
    public List<OrderAndUserDTO> getOrdersByStatus(@Argument Integer status_min, @Argument Integer status_max) {
        if (status_min <= status_max){
            return service.getOrdersByStatus(status_min, status_max).join();
        } else {
            throw new IllegalArgumentException("'status_min' must be less than 'status_max' or equal");
        }
    }

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @QueryMapping
    public List<CartGoodDTO> getCart(){
        return service.getCart().join();
    }

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @MutationMapping
    public String addGoodsToCart(@Argument List<Long> ids){
        if(ids.isEmpty()){
            throw new EmptyListException("Provided list must contain elements");
        } else {
            CompletableFuture<String> future = service.addGoodsToCart(ids);
            return future.join();
        }
    }

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @MutationMapping
    public String delGoodsFromCart(@Argument List<Long> ids){
        if(ids.isEmpty()){
            throw new EmptyListException("Provided list must contain elements");
        } else{
            CompletableFuture<String> future = service.delGoodsFromCart(ids);
            return future.join();
        }
    }

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @MutationMapping
    public String alterCartGoodAmounts(@Argument List<GoodAmountDTO> goods){
        if(goods.isEmpty()){
            throw new EmptyListException("Provided list must contain elements");
        } else{
            CompletableFuture<String> future = service.alterCartGoodsAmount(goods);
            return future.join();
        }
    }

    @PreAuthorize("hasAuthority('OWNER')")
    @MutationMapping
    public String setOrderStatus(@Argument Long id, @Argument Integer status){
        return service.setOrderStatus(id, status).join();
    }

    @PreAuthorize("hasAuthority('CUSTOMER')")
    @MutationMapping
    public String createOrder(@Argument Boolean buyAvailable){
        return service.createOrder(buyAvailable);
    }
}
