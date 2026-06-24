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
@Schema(description = "관광지 목록 조회 응답")
public class TouristSpotListResponse {
    
    @Schema(description = "관광지 ID", example = "1")
    private Long id;
    
    @Schema(description = "관광지명", example = "경복궁")
    private String name;
    
    @Schema(description = "위도", example = "37.579617")
    private Double latitude;
    
    @Schema(description = "경도", example = "126.977041")
    private Double longitude;
    
    @Schema(description = "주소", example = "서울 종로구 사직로 161")
    private String address;
    
    @Schema(description = "설명", example = "조선시대 대표 궁궐로, 한국의 전통 건축미를 감상할 수 있는 역사적인 장소입니다.")
    private String description;
    
    @Schema(description = "썸네일 URL", example = "https://example.com/gyeongbokgung.jpg")
    private String thumbnailUrl;
    
    @Schema(description = "카테고리", example = "역사문화")
    private String category;
}
