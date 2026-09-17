package com.tourfolio.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "수집 미션 보상 수령 응답 DTO")
public class MissionClaimResponse {

    @Schema(description = "미션 ID", example = "1")
    private Long missionId;

    @Schema(description = "완료 여부", example = "true")
    private Boolean isCompleted;

    @Schema(description = "이미 보상을 받은 상태였는지 여부 (재요청 시 true)", example = "false")
    private Boolean alreadyRewarded;

    @Schema(description = "이번 요청으로 지급된 포인트 (재요청 시 0)", example = "20000")
    private Integer pointsAwarded;

    @Schema(description = "지급 후 보유 포인트 잔액", example = "50000")
    private BigDecimal balance;
}