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
@Schema(description = "테마 카드 DTO")
public class ThemeCard {

    @Schema(description = "테마 ID", example = "1")
    private Long themeId;

    @Schema(description = "테마 제목", example = "벚꽃 따라 떠나는 봄 여행")
    private String title;

    @Schema(description = "장소 개수", example = "12")
    private Integer placeCount;

    @Schema(description = "테마 썸네일 이미지 URL", example = "https://example.com/images/cherry-blossom.jpg")
    private String imageUrl;
}
