package com.example.onlineshop.controller;

import com.example.onlineshop.exception.EmptyListException;
import com.example.onlineshop.model.dto.AlterGoodDTO;
import com.example.onlineshop.model.dto.GoodAddDTO;
import com.example.onlineshop.model.dto.GoodDTO;
import com.example.onlineshop.service.GoodsService;
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
public class GoodsController {
    GoodsService service;

    @QueryMapping
    public List<GoodDTO> getGoodsList(){
        return service.getGoodsList().join();
    }

    @QueryMapping
    public GoodDTO getGood(@Argument Long id){
        return service.getGood(id).join();
    }

    @QueryMapping
    public List<GoodDTO> findGoods(@Argument String name){
        return service.findGoods(name).join();
    }

    @PreAuthorize("hasAuthority('OWNER')")
    @MutationMapping
    public String addGood(@Argument List<GoodAddDTO> goods) {
        if(goods.isEmpty()){
            throw new EmptyListException("Provided list must contain elements");
        } else if (goods.size() == 1) {
            CompletableFuture<String> future = service.addGood(goods.get(0));
            return future.join();
        }else{
            CompletableFuture<String> future = service.addGoods(goods);
            return future.join();
        }
    }

    @PreAuthorize("hasAuthority('OWNER')")
    @MutationMapping
    public String alterGood(@Argument List<AlterGoodDTO> goods) {
        if(goods.isEmpty()){
            throw new EmptyListException("Provided list must contain elements");
        } else if (goods.size() == 1) {
            CompletableFuture<String> future = service.alterGood(goods.get(0));
            return future.join();
        }else{
            CompletableFuture<String> future = service.alterGoods(goods);
            return future.join();
        }
    }
}
