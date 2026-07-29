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
@Schema(description = "테마별 관광지 목록 조회 응답 DTO")
public class ThemeDetailResponse {

    @Schema(description = "테마 ID", example = "1")
    private Long themeId;

    @Schema(description = "테마 제목", example = "벚꽃 따라 떠나는 봄 여행")
    private String title;

    @Schema(description = "테마 설명", example = "전국의 벚꽃 명소를 소개하는 테마 여행")
    private String description;

    @Schema(description = "테마 썸네일 이미지 URL", example = "https://example.com/images/cherry-blossom.jpg")
    private String imageUrl;

    @Schema(description = "포함된 관광지 목록")
    private List<ThemeSpotSummary> spots;
}
