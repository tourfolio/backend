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
@Schema(description = "GPS 기반 카드 획득 요청 DTO")
public class AcquireCardRequest {

    @Schema(description = "현재 위도", example = "35.1587", required = true)
    private Double latitude;

    @Schema(description = "현재 경도", example = "129.1604", required = true)
    private Double longitude;

    @Schema(description = "관광지와의 거리 (미터 단위)", example = "150.5", required = true)
    private Double distanceInMeters;
}
