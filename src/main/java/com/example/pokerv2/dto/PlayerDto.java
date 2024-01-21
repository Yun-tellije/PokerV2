package com.example.pokerv2.dto;

import com.example.pokerv2.model.Player;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDto {

    private Long id;
    private Long userId;
    private String playerName;
    private Long boardId;
    private int position;
    private int money;
    private int card1;
    private int card2;
    private int status;
    private int totalCallSize;
    private int phaseCallSize;
    private GameResultDto gameResult;

    public PlayerDto(Player player) {
        this.id = player.getId();
        this.userId = player.getUser().getId();
        this.playerName = player.getUser().getUsername();
        this.boardId = player.getBoard().getId();
        this.position = player.getPosition().ordinal();
        this.money = player.getMoney();
        this.card1 = player.getCard1();
        this.card2 = player.getCard2();
        this.status = player.getStatus().ordinal();
        this.totalCallSize = player.getTotalCallSize();
        this.phaseCallSize = player.getPhaseCallSize();
    }

}