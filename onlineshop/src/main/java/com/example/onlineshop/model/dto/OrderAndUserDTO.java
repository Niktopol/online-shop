package com.example.onlineshop.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class OrderAndUserDTO {
    Long id;
    List<OrderGoodDTO> goods;
    Integer status;
    Double price;
    Long userId;
}
