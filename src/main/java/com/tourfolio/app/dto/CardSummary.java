package com.tourfolio.app.dto;

import com.tourfolio.app.entity.Card;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "수집 메인 화면용 카드 요약 DTO")
public class CardSummary {

    @Schema(description = "카드 ID", example = "1")
    private Long cardId;

    @Schema(description = "관광지명", example = "경복궁")
    private String spotName;

    @Schema(description = "이미지 URL", example = "https://example.com/images/gyeongbokgung.jpg")
    private String imageUrl;

    @Schema(description = "희귀도", example = "EPIC")
    private Card.CardRarity rarity;

    @Schema(description = "테마", example = "역사")
    private String theme;

    @Schema(description = "지역", example = "서울")
    private String region;

    @Schema(description = "보유 여부", example = "true")
    private Boolean isOwned;
}
