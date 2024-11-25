package com.example.onlineshop.model.dto;

import com.example.onlineshop.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class UserInfoDTO {
    private Long id;
    private String name;
    private String username;
    private boolean enabled;

    public UserInfoDTO(User user){
        this.id = user.getId();
        this.name = user.getName();
        this.username = user.getUsername();
        this.enabled = user.isEnabled();
    }
    public UserInfoDTO(){
        this.id = -1L;
        this.name = "";
        this.username = "";
    }
}
