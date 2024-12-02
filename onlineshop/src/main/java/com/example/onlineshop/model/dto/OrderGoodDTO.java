package com.example.onlineshop.model.dto;

import com.example.types.Good;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class OrderGoodDTO {
    GoodDTO good;
    Double price;
    Integer amount;
}
