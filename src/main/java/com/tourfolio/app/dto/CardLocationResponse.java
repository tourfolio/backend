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
@Schema(description = "카드(관광지) 좌표 응답 DTO")
public class CardLocationResponse {

    @Schema(description = "카드 ID", example = "1")
    private Long cardId;

    @Schema(description = "관광지명", example = "경복궁")
    private String spotName;

    @Schema(description = "위도", example = "37.57603072")
    private Double latitude;

    @Schema(description = "경도", example = "126.9767219")
    private Double longitude;
}