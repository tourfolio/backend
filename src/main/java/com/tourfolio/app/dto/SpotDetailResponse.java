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
@Schema(description = "관광지 상세 정보 조회 응답 DTO")
public class SpotDetailResponse {

    @Schema(description = "관광지 ID", example = "1")
    private Long spotId;

    @Schema(description = "관광지명", example = "경복궁")
    private String name;

    @Schema(description = "대표 이미지 URL", example = "https://tong.visitkorea.or.kr/cms/resource/98/3487598_image2_1.jpg")
    private String imageUrl;

    @Schema(description = "위치/주소", example = "서울 종로구 사직로 161")
    private String address;

    @Schema(description = "대표 태그 목록", example = "[\"역사\", \"문화\", \"야경\", \"가족여행\"]")
    private List<String> tags;

    @Schema(description = "상세 소개글", example = "조선 왕조의 법궁으로, 500년의 역사가 살아 숨 쉬는 곳입니다.")
    private String description;

    @Schema(description = "운영시간", example = "09:00 - 18:00 (계절별 변동)")
    private String operatingHours;

    @Schema(description = "휴무일", example = "매주 화요일")
    private String closedDays;

    @Schema(description = "입장료", example = "성인 3,000원")
    private String admissionFee;

    @Schema(description = "공식 웹사이트", example = "https://www.royalpalace.go.kr")
    private String website;

    @Schema(description = "전화번호", example = "02-3700-1114")
    private String phoneNumber;

    @Schema(description = "매력 포인트 리스트")
    private List<AttractionPoint> attractionPoints;

    @Schema(description = "주변 관광지 목록")
    private List<NearbySpot> nearbySpots;


}
