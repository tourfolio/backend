package com.tourfolio.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "홈 화면 통합 응답 DTO")
public class HomeResponse {

    private PortfolioSummary portfolio;
    private CardCollectionSummary cardCollection;
    private List<RecommendedSpotItem> recommendedSpots;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortfolioSummary {
        private BigDecimal totalAsset;
        private BigDecimal todayProfit;
        private BigDecimal todayProfitRate;
        private BigDecimal totalProfitRate;
        private Integer stockCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardCollectionSummary {
        private Long ownedCount;
        private Long totalCount;
        private Double collectionRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedSpotItem {
        private Long spotId;
        private String name;
        private String imageUrl;
        private String description;
        private List<String> tags;
    }
}