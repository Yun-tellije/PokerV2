package com.example.pokerv2.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 2024/01/02 jungeun
 *
 * 1. HUD(heads-up display)
 * 본인과 상대의 데이터를 확인하는 디스플레이
 *
 * 2. totalHands
 * ?????????
 * ---------------
 * 수정
 * 게임에 참여한 총 횟수
 *
 * 3. vpip
 * 자발적으로 플랍에 참전한 정도
 *
 * 4. pfr
 * 프리플랍에서 얼마나 레이즈를 하는지 비율
 *
 * 5. cBet
 * 프리플랍 레이즈가 플랍에서 벳하는 것을 C벳이라고 한다.
 * C벳의 비율을 나타낸다.
 *
 * 6. threeBet
 * 3벳의 비율을 나타낸다.
 *
 * 7. wtsd
 * 쇼다운까지 가는 정도
 *
 * 8. wsd
 * 쇼다운에서 이기는 정도
 *
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class Hud {

    @Id @GeneratedValue
    private Long id;
    @OneToOne
    private User user;
    private int vpip;
    private int pfr;
    private int cBet;
    private int threeBet;
    private int wtsd;
    private int wsd;
    private int totalHands;
    private int pfAggressiveCnt;
    private int wtf;


}