package com.example.pokerv2.dto;

import com.example.pokerv2.model.Hud;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HudDto {


    private Long id;
    private Long userId;
    private String userName;
    private int vpip;
    private int pfr;
    private int cBet;
    private int threeBet;
    private int wtsd;
    private int wsd;
    private int totalHands;
    private int pfAggressiveCnt;
    private int wtf;

    public HudDto(Hud hud) {
        this.id = hud.getId();
        this.userName = hud.getUser().getUsername();
        this.userId = hud.getUser().getId();
        this.totalHands = hud.getTotalHands();
        this.vpip = hud.getVpip();
        this.pfr = hud.getPfr();
        this.cBet = hud.getCBet();
        this.threeBet = hud.getThreeBet();
        this.wtsd = hud.getWtsd();
        this.wsd = hud.getWsd();
        this.pfAggressiveCnt = hud.getPfAggressiveCnt();
        this.wtf = hud.getWtf();
    }
}