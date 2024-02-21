package com.example.pokerv2.model;

import com.example.pokerv2.dto.BoardDto;
import com.example.pokerv2.enums.PhaseStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/**
 * 2024/01/02 jungeun
 *
 * 1. totalPlayer
 * 총 플레이어의 수를 나타낸다.
 *
 * 2. btn
 * 액션 순서 및 포지션을 정해줌
 *
 * 3. blind
 * 게임의 매 판마다 지불하는 입장료
 *
 * 4. pot
 * 게임의 판돈
 *
 * 5. bettingPos
 * 베팅이 나온 포지션
 *
 * 6. actionPos
 * 액션을 물어봐야하는 포지션
 *
 * 7. phaseNum
 * phase 상태를 나타냄?
 *
 * 8. bettingSize
 * 베팅 금액
 *
 * 9. communityCard1~5
 * 공유하는 카드들의 번호 저장
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Board {

    @Id @GeneratedValue
    private Long id;
    private Long gameSeq;
    private int btn;
    private int totalPlayer;
    private int blind;
    private int pot;
    private int bettingPos;
    private int actionPos;

    @Enumerated(EnumType.ORDINAL)
    @Builder.Default
    private PhaseStatus phaseStatus = PhaseStatus.WAITING;
    private int bettingSize;

    private int communityCard1;
    private int communityCard2;
    private int communityCard3;
    private int communityCard4;
    private int communityCard5;
    private LocalDateTime lastActionTime;

    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @OrderBy("position asc")
    @Builder.Default
    private List<Player> players = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @Builder.Default
    private List<Integer> totalCallSize = new ArrayList<>(List.of(0,0,0,0,0,0));

}