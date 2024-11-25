package com.example.onlineshop.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class UserDTO {
    private String name;
    private String username;
    private String password;

    public UserDTO(){
        this.name = "";
        this.username = "";
        this.password = "";
    }
}
