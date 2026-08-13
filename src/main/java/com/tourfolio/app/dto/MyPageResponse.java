package com.tourfolio.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "마이페이지 요약 DTO")
public class MyPageResponse {
    private String nickname;
    private BigDecimal balance;
    private Long cardCount;
    private BigDecimal totalProfitRate; // %
}