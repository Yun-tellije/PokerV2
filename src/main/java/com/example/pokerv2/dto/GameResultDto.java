package com.example.pokerv2.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Builder
public class GameResultDto {

    /**
     * GameResultDto
     *
     * 게임이 끝났을 때 게임의 결과를 알려줘야 함.
     *
     * 표시해야 하는 정보들
     * 1. 누가 이겼는지.
     *
     * 2. 얼마를 벌었는지.
     * 승자가 한명일 때도 있지만 여러명인 상황도 있다.
     * 승자가 여러명인 상황에서는 팟을 나눠먹어야 하기 때문에 모든 상황에서 팟의 전체금액을 한명의 승자가 독차지 하는 것이 아니다.
     * 그래서 얼마를 벌었는지 각 플레이어마다 써놔야 한다.
     *
     * 3. 각 플레이어들의 족보.
     *
     */

    private boolean isWinner;
    private int earnedMoney;
    private String handContext;
    private long handValue;
    private List<Integer> jokBo;

}