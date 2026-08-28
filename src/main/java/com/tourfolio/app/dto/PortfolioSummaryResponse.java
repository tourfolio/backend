// src/main/java/com/tourfolio/app/dto/PortfolioSummaryResponse.java
package com.tourfolio.app.dto;

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
public class PortfolioSummaryResponse {
    private BigDecimal totalAsset;        // 총 평가금액 (현금 + 주식)
    private BigDecimal totalEvaluation;   // 주식 평가금액
    private BigDecimal totalPurchase;    // 총 매수금액
    private BigDecimal totalProfitLoss;   // 총 평가손익
    private BigDecimal profitRate;        // 수익률 (%)
    private BigDecimal cashBalance;       // 보유 현금
    private BigDecimal monthlyProfit;      // 이번달 실현손익 (원)
    private BigDecimal monthlyProfitRate;  // 이번달 실현손익률 (%)
    private List<AssetHistoryItem> assetHistory; // 자산 추이 데이터

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssetHistoryItem {
        private String date;              // MM/dd 형식
        private BigDecimal totalAsset;     // 해당 날짜의 총 자산
    }
}