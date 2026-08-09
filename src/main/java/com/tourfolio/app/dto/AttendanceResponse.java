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
@Schema(description = "출석 체크 응답 DTO")
public class AttendanceResponse {

    @Schema(description = "지급된 포인트", example = "1000")
    private BigDecimal pointsAwarded;

    @Schema(description = "현재 잔액", example = "51000")
    private BigDecimal currentBalance;

    @Schema(description = "메시지", example = "출석체크 완료! 1,000P가 지급되었습니다.")
    private String message;
}
