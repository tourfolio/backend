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
@Schema(description = "매력 포인트 DTO")
public class AttractionPoint {

    @Schema(description = "매력 포인트 제목", example = "야경 감상")
    private String title;

    @Schema(description = "아이콘 타입", example = "moon")
    private String iconType;

    @Schema(description = "아이콘 URL", example = "https://example.com/icons/moon.png")
    private String iconUrl;
}
