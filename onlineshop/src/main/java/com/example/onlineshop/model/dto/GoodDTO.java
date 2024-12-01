package com.example.onlineshop.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class GoodDTO {
    Long id;
    String name;
    Double price;
    Integer amount;
    Boolean canBeSold;
}
