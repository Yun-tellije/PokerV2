package com.example.pokerv2.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum HandValue {
    ROYAL_STRAIGHT_FLUSH("로얄 스트레이트 플러쉬"),
    STRAIGHT_FLUSH("스트레이트 플러쉬"),
    FOUR_OF_A_KIND("포카드"),
    FULL_HOUSE("풀하우스"),
    FLUSH("플러쉬"),
    STRAIGHT("스트레이트"),
    THREE_OF_A_KIND("트리플"),
    TWO_PAIR("투페어"),
    ONE_PAIR("원페어"),
    HIGH_CARD("하이카드")
    ;

    private final String detail;

}