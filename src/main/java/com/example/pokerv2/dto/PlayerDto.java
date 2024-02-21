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
        this.phaseCallSize = player.getPhaseCallSize();
    }

    public PlayerDto(PlayerDto playerDto) {
        this.id = playerDto.getId();
        this.userId = playerDto.getUserId();
        this.playerName = playerDto.getPlayerName();
        this.boardId = playerDto.getBoardId();
        this.position = playerDto.getPosition();
        this.money = playerDto.getMoney();
        this.card1 = playerDto.getCard1();
        this.card2 = playerDto.getCard2();
        this.status = playerDto.getStatus();
        this.phaseCallSize = playerDto.getPhaseCallSize();
        this.gameResult = playerDto.getGameResult();
    }

}