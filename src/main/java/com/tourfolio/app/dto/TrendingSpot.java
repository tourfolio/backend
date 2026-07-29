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
@Schema(description = "인기/급상승 관광지 DTO")
public class TrendingSpot {

    @Schema(description = "관광지 ID", example = "1")
    private Long spotId;

    @Schema(description = "관광지 이름", example = "해운대 엘시티")
    private String name;

    @Schema(description = "지역명", example = "부산")
    private String location;

    @Schema(description = "주간 상승률", example = "+32%")
    private String weeklyChangeRate;

    @Schema(description = "썸네일 이미지 URL", example = "https://example.com/images/haeundae.jpg")
    private String imageUrl;
}
