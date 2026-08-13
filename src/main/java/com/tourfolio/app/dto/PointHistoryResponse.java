package com.tourfolio.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "포인트 내역 DTO")
public class PointHistoryResponse {

    private BigDecimal balance;
    private List<PointHistoryItem> histories;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PointHistoryItem {
        private String title;
        private Long amount;
        private LocalDateTime createdAt;
    }
}