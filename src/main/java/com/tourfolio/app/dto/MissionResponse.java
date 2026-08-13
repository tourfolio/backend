package com.tourfolio.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "미션 카드 DTO")
public class MissionResponse {

    private Long missionId;
    private String category;      // VISIT, COLLECT, INVEST, ATTENDANCE
    private String title;
    private Integer rewardPoints;
    private Integer currentProgress;
    private Integer conditionTarget;
    private Boolean isCompleted;
}