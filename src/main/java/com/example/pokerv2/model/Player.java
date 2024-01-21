package com.example.pokerv2.model;

import com.example.pokerv2.dto.PlayerDto;
import com.example.pokerv2.enums.PlayerStatus;
import com.example.pokerv2.enums.Position;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 2024/01/02 jungeun
 *
 * 1. user
 * 유저 정보 가져옴
 *
 * 2. bb
 * ????????????
 * --------------
 * 수정
 * 칩 단위
 *
 * 3. card1, 2
 * 플레이어가 가지고 있는 카드 정보
 *
 * 4. status
 * *************
 * 여기 혹시 PlayStatus를 사용 안하고 int를 쓰나요?
 *
 * 5. totalCallSize
 * ????????????
 * -------------------
 * 수정
 * 사이드팟이 진행될 때 사용
 *
 */

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class Player {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "board_id")
    private Board board;

    private Position position;

    private int money;
    private int card1;
    private int card2;
    private PlayerStatus status;
    private int totalCallSize;
    private int phaseCallSize;

    public void changePlayerStatus(PlayerDto playerDto) {
        this.money = playerDto.getMoney();
        this.status = PlayerStatus.valueOf(playerDto.getStatus());
        this.phaseCallSize = playerDto.getPhaseCallSize();
    }
}