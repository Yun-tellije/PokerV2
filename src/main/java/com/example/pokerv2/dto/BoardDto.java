package com.example.pokerv2.dto;

import com.example.pokerv2.model.Board;
import com.example.pokerv2.model.Player;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardDto {

    private Long id;
    private int totalPlayer;
    private int blind;
    private int btn;
    private int pot;
    private int bettingPos;
    private int actionPos;
    private int phaseStatus;
    private int bettingSize;
    private int communityCard1;
    private int communityCard2;
    private int communityCard3;
    private int communityCard4;
    private int communityCard5;
    private LocalDateTime lastActionTime;
    private List<Integer> totalCallSize;
    private List<PlayerDto> players;

    public BoardDto(Board board) {
        this.id = board.getId();
        this.totalPlayer = board.getTotalPlayer();
        this.btn = board.getBtn();
        this.blind = board.getBlind();
        this.pot = board.getPot();
        this.bettingPos = board.getBettingPos();
        this.actionPos = board.getActionPos();
        this.phaseStatus = board.getPhaseStatus().ordinal();
        this.bettingSize = board.getBettingSize();
        this.communityCard1 = board.getCommunityCard1();
        this.communityCard2 = board.getCommunityCard2();
        this.communityCard3 = board.getCommunityCard3();
        this.communityCard4 = board.getCommunityCard4();
        this.communityCard5 = board.getCommunityCard5();
        List<Player> pList = board.getPlayers();
        List<PlayerDto> pDtoList = new ArrayList<>();
        for (Player player : pList) {
            pDtoList.add(new PlayerDto(player));
        }
        this.totalCallSize = board.getTotalCallSize();
        this.lastActionTime = board.getLastActionTime();
        this.players = pDtoList;
    }
}
