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
@Schema(description = "주변 관광지 DTO")
public class NearbySpot {

    @Schema(description = "관광지 ID", example = "2")
    private Long spotId;

    @Schema(description = "관광지 이름", example = "창덕궁")
    private String name;

    @Schema(description = "이미지 URL", example = "https://example.com/images/changdeokgung.jpg")
    private String imageUrl;
}
