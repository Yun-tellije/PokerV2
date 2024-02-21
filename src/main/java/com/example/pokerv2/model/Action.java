package com.example.pokerv2.model;

import com.example.pokerv2.enums.PhaseStatus;
import com.example.pokerv2.enums.Position;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 2024/01/02 jungeun
 *
 * 1. actionNo
 * 액션의 순서가 정해진다
 * ex) 처음으로 시작했을 때 UTG가 1, MP가 2 ... BB가 6
 * -------------------------------------------------
 * 수정
 *
 *
 * 2. position
 * Position 배열에 있는 것들 중에서 현재 상태가 지정된다.
 * BTN(딜러 버튼) -> SB(스몰 블라인드) -> BB(빅 블라인드) -> UTG(언더 더 건) -> MP(미들 포지션) -> CO(컷 오프)
 * 딜러를 기준으로 시계방향으로 포지션이 지정된다.
 * 베팅 및 액션의 순서를 결정한다.
 * 시작은 UTG부터, BB가 마지막으로 행동한다.
 * --------------------------------------
 * 수정
 * 액션이 어떤 포지션에서 나왔는지 저장
 *
 * 3. phaseStatus
 * 현재 게임의 상태를 나타낸다.
 *
 *
 * 4. detail
 * ?????????????????
 *
 *
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Action {

    @Id @GeneratedValue
    private Long id;
    private int actionNo;
    private Long userId;
    private int position;
    @Enumerated(EnumType.ORDINAL)
    private PhaseStatus phaseStatus;
    @Builder.Default
    private String detail ="";
    @ManyToOne
    private HandHistory handHistory;

}