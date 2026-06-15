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
@Schema(description = "지도 마커 조회 응답")
public class TouristSpotMapResponse {
    
    @Schema(description = "관광지 ID", example = "1")
    private Long id;
    
    @Schema(description = "관광지명", example = "경복궁")
    private String name;
    
    @Schema(description = "위도", example = "37.579617")
    private Double latitude;
    
    @Schema(description = "경도", example = "126.977041")
    private Double longitude;
}
