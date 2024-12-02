package com.example.onlineshop.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class CartGoodDTO {
    GoodDTO good;
    Integer amount;
}