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
@Schema(description = "탐색 화면 풀스크린 메인 카드 응답 DTO (Editor's Pick)")
public class MainCardResponse {

    @Schema(description = "관광지 ID", example = "1")
    private Long spotId;

    @Schema(description = "관광지 이름", example = "경복궁")
    private String name;

    @Schema(description = "서브 카피", example = "조선의 시간을 품은 공간")
    private String subTitle;

    @Schema(description = "상세 설명", example = "500년의 역사가 살아 숨 쉬는 곳")
    private String description;

    @Schema(description = "위치/지역명", example = "서울 종로구")
    private String location;

    @Schema(description = "풀스크린 배경 이미지 URL", example = "https://example.com/images/gyeongbokgung.jpg")
    private String imageUrl;

    @Schema(description = "전체 카드 개수", example = "6")
    private Integer totalCount;

    @Schema(description = "카드 순번", example = "1")
    private Integer currentIndex;
}
