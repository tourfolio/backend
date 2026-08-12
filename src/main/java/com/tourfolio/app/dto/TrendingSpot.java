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
@Schema(description = "인기/추천 관광지 DTO")
public class TrendingSpot {

    @Schema(description = "관광지 ID", example = "1")
    private Long spotId;

    @Schema(description = "관광지 이름", example = "해운대 엘시티")
    private String name;

    @Schema(description = "지역명", example = "부산")
    private String location;

    @Schema(description = "인기 순위", example = "1")
    private Integer popularityRank;

    @Schema(description = "썸네일 이미지 URL", example = "https://example.com/images/haeundae.jpg")
    private String imageUrl;

    @Schema(description = "상세 주소", example = "부산광역시 해운대구 해운대해변로 264")
    private String address;
}
