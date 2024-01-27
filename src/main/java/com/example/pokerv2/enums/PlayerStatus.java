package com.example.pokerv2.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PlayerStatus {


    DISCONNECT_FOLD(0),
    DISCONNECT_PLAYED(1),
    DISCONNECT_ALL_IN(2),
    FOLD(3),
    ALL_IN(4),
    PLAY(5);

    private final int statusNum;

    public static PlayerStatus valueOf(int statusNum) {
        for (PlayerStatus status : PlayerStatus.values()) {
            if (status.getStatusNum() == statusNum) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid statusNum: " + statusNum);
    }
}