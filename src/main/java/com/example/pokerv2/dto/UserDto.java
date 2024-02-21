package com.example.pokerv2.dto;

import com.example.pokerv2.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {


    private Long id;
    private String userId;
    private String userName;
    private int money;
    private HudDto hud;
    private String imagePath;

    public UserDto(User user){
        this.id = user.getId();
        this.userId = user.getUserId();
        this.userName = user.getUsername();
        this.money = user.getMoney();
        this.hud = new HudDto(user.getHud());
        this.imagePath = user.getImagePath();
    }
}