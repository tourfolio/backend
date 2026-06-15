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
@Schema(description = "관광지 검색 응답")
public class TouristSpotSearchResponse {
    
    @Schema(description = "관광지 ID", example = "1")
    private Long id;
    
    @Schema(description = "관광지명", example = "경복궁")
    private String name;
}
