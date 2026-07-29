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
@Schema(description = "포토카드 상세 조회 응답 DTO")
public class CardDetailResponse {

    @Schema(description = "카드 ID", example = "1")
    private Long cardId;

    @Schema(description = "카드명 (한글/영문)", example = "경복궁 / Gyeongbokgung Palace")
    private String name;

    @Schema(description = "주소", example = "서울 종로구 사직로 161")
    private String address;

    @Schema(description = "희귀도", example = "EPIC")
    private Card.CardRarity rarity;

    @Schema(description = "테마", example = "역사")
    private String theme;

    @Schema(description = "이미지 URL", example = "https://example.com/images/gyeongbokgung.jpg")
    private String imageUrl;

    @Schema(description = "희귀도 글로우 색상 코드", example = "#9B59B6")
    private String glowColorCode;

    @Schema(description = "카드 번호", example = "No.08")
    private String cardNumber;

    @Schema(description = "카드 뒷면 감성 한 줄 문구", example = "500년의 역사가 숨 쉬는 곳")
    private String phrase;

    @Schema(description = "보유 여부", example = "true")
    private Boolean isOwned;

    @Schema(description = "획득일 (YYYY.MM.DD)", example = "2024.07.29")
    private String acquiredAt;

    @Schema(description = "획득 경로", example = "관광지 방문")
    private String acquisitionPath;

    @Schema(description = "안내 문구 (미보유 시)", example = "관광지를 직접 방문하여 카드를 획득하세요!")
    private String message;
}
