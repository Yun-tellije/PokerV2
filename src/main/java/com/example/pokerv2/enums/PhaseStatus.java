package com.example.pokerv2.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PhaseStatus {

    WAITING,
    PRE_FLOP,
    FLOP,
    TURN,
    RIVER,
    END_GAME,
    SHOWDOWN;

    public static PhaseStatus valueOf(int phaseNum) {
        for (PhaseStatus status : PhaseStatus.values()) {
            if (status.ordinal() == phaseNum) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid phaseNum: " + phaseNum);
    }
}