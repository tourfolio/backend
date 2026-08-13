package com.tourfolio.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "업적 메인 화면 DTO")
public class MissionListResponse {

    private BigDecimal balance;
    private List<Boolean> weeklyAttendance; // 월~일 순서, 7개
    private Boolean attendedToday;
    private Integer inProgressCount;
    private Integer completedCount;
    private List<MissionResponse> missions;
}