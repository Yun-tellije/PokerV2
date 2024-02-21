package com.example.pokerv2.dto;

import com.example.pokerv2.enums.PhaseStatus;
import com.example.pokerv2.enums.Position;
import com.example.pokerv2.model.Action;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionDto {

    private Long id;
    private int actionNo;
    private Long userId;

    private int actPosition;
    private int phaseStatus;
    private String detail;

    public ActionDto(Action action) {
        this.id = action.getId();
        this.actionNo = action.getActionNo();
        this.userId = action.getUserId();
        this.actPosition = action.getPosition();
        this.phaseStatus = action.getPhaseStatus().ordinal();
        this.detail = action.getDetail();
    }
}