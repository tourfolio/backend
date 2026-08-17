package com.tourfolio.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "수집 메인 화면 응답 DTO")
public class CollectionResponse {

    @Schema(description = "전체 카드 기준 수집 요약 (필터와 무관하게 항상 전체 기준으로 계산됨)")
    private CollectionSummary summary;

    @Schema(description = "현재 필터 조건에 해당하는 카드 개수", example = "2")
    private Integer filteredCount;

    @Schema(description = "필터링된 카드 목록")
    private List<CardSummary> cards;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "전체 수집 요약")
    public static class CollectionSummary {

        @Schema(description = "전체 수집률 (%)", example = "20.0")
        private Double collectionRate;

        @Schema(description = "전체 기준 보유 카드 수", example = "2")
        private Integer ownedCount;

        @Schema(description = "전체 카드 수", example = "10")
        private Integer totalCount;
    }
}