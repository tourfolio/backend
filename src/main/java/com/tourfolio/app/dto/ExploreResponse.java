// src/main/java/com/tourfolio/app/dto/ExploreResponse.java
package com.tourfolio.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "탐색 화면용 관광지 카드 응답 DTO")
public class ExploreResponse {

    @Schema(description = "관광지 고유 ID", example = "1")
    private Long id;

    @Schema(description = "관광지명", example = "부산 해운대 엘시티")
    private String name;

    @Schema(description = "지역 코드", example = "6")
    private String areaCode;

    @Schema(description = "화면 표시용 지역명", example = "부산")
    private String areaName;

    @Schema(description = "테마 태그", example = "레저")
    private String themeTag;

    @Schema(description = "티어 등급", example = "1")
    private Integer tier;

    @Schema(description = "대표 이미지 URL", example = "https://example.com/images/spot1.jpg")
    private String imageUrl;

    @Schema(description = "관광지 요약 설명", example = "해운대의 랜드마크 쇼핑몰과 호텔 복합 시설")
    private String description;
}
