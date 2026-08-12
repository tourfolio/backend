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
@Schema(description = "카드 획득 응답 DTO")
public class AcquireCardResponse {

    @Schema(description = "카드 ID", example = "1")
    private Long cardId;

    @Schema(description = "카드명(관광지명)", example = "경복궁")
    private String cardName;

    @Schema(description = "희귀도", example = "EPIC")
    private Card.CardRarity rarity;

    @Schema(description = "획득일자 (YYYY.MM.DD)", example = "2026.08.11")
    private String acquiredAt;
}