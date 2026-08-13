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
@Schema(description = "출석체크 응답 DTO")
public class AttendanceCheckResponse {

    private Integer pointsAwarded;
    private BigDecimal balance;
    private Integer consecutiveDays;
}