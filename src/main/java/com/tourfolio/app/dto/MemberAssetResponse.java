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
public class MemberAssetResponse {
    private Long memberId;
    private String username;
    private BigDecimal cashBalance;       // 가상 현금 보유액
    private BigDecimal totalStockValue;    // 보유한 주식들의 평가 금액 총합
    private BigDecimal totalAssetValue;    // 현금 + 주식 총 자산
    private BigDecimal totalProfitLossRate; // 총 투자 수익률 (%)
    private List<AssetItem> items;         // 보유 종목 상세 상세

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssetItem {
        private Long spotId;
        private String spotName;
        private BigDecimal quantity;
        private BigDecimal averagePurchasePrice;
        private BigDecimal currentPrice;
        private BigDecimal evaluationAmount;  // 현재가 * 수량
        private BigDecimal profitLossRate;     // 해당 종목 수익률
    }
}