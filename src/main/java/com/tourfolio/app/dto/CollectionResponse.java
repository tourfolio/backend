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

    @Schema(description = "수집률 (%)", example = "27.0")
    private Double collectionRate;

    @Schema(description = "보유한 카드 수", example = "58")
    private Integer ownedCount;

    @Schema(description = "전체 카드 수", example = "210")
    private Integer totalCount;

    @Schema(description = "카드 목록")
    private List<CardSummary> cards;
}
