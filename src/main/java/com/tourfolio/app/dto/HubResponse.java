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
@Schema(description = "컨텐츠 허브 조회 응답 DTO")
public class HubResponse {

    @Schema(description = "테마로 떠나는 여행 목록")
    private List<ThemeCard> themes;

    @Schema(description = "지금 뜨는 여행지 (인기/급상승 관광지)")
    private List<TrendingSpot> trendingSpots;
}
