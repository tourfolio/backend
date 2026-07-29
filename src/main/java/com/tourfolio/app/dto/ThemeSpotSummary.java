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
@Schema(description = "테마별 관광지 요약 DTO")
public class ThemeSpotSummary {

    @Schema(description = "관광지 ID", example = "1")
    private Long spotId;

    @Schema(description = "관광지 이름", example = "경복궁")
    private String name;

    @Schema(description = "위치/지역명", example = "서울 종로구")
    private String location;

    @Schema(description = "이미지 URL", example = "https://example.com/images/gyeongbokgung.jpg")
    private String imageUrl;

    @Schema(description = "태그 목록", example = "[\"역사\", \"문화\", \"야경\"]")
    private List<String> tags;
}
