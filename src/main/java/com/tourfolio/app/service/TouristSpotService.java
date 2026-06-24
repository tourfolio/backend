package com.tourfolio.app.service;

import com.tourfolio.app.dto.TouristSpot;
import com.tourfolio.app.dto.TouristSpotDetailResponse;
import com.tourfolio.app.dto.TouristSpotListResponse;
import com.tourfolio.app.dto.TouristSpotMapResponse;
import com.tourfolio.app.dto.TouristSpotSearchResponse;
import com.tourfolio.app.exception.TouristSpotNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TouristSpotService {

    private List<TouristSpot> touristSpots = new ArrayList<>();

    @PostConstruct
    public void init() {
        log.info("Initializing tourist spots data...");
        
        touristSpots = List.of(
            TouristSpot.builder()
                .id(1L)
                .name("경복궁")
                .latitude(37.579617)
                .longitude(126.977041)
                .address("서울 종로구 사직로 161")
                .description("조선시대 대표 궁궐로, 한국의 전통 건축미를 감상할 수 있는 역사적인 장소입니다.")
                .thumbnailUrl("https://example.com/gyeongbokgung.jpg")
                .category("역사문화")
                .build(),
            TouristSpot.builder()
                .id(2L)
                .name("창덕궁")
                .latitude(37.579617)
                .longitude(126.991066)
                .address("서울 종로구 율곡로 99")
                .description("유네스코 세계문화유산으로 지정된 조선시대 궁궐로, 비원과 함께 아름다운 정원을 자랑합니다.")
                .thumbnailUrl("https://example.com/changdeokgung.jpg")
                .category("역사문화")
                .build(),
            TouristSpot.builder()
                .id(3L)
                .name("N서울타워")
                .latitude(37.551169)
                .longitude(126.988226)
                .address("서울 용산구 남산공원길 105")
                .description("서울의 랜드마크로, 남산 정상에 위치하여 서울 전경을 한눈에 조망할 수 있습니다.")
                .thumbnailUrl("https://example.com/nseoultower.jpg")
                .category("관광명소")
                .build(),
            TouristSpot.builder()
                .id(4L)
                .name("북촌한옥마을")
                .latitude(37.583801)
                .longitude(126.983647)
                .address("서울 종로구 계동길 37")
                .description("전통 한옥이 보존된 곳으로, 한국의 전통 주거 문화를 체험할 수 있는 곳입니다.")
                .thumbnailUrl("https://example.com/bukchon.jpg")
                .category("역사문화")
                .build(),
            TouristSpot.builder()
                .id(5L)
                .name("청계천")
                .latitude(37.570536)
                .longitude(126.979522)
                .address("서울 종로구 청계천로 100")
                .description("도심 속의 자연을 느낄 수 있는 곳으로, 복원된 청계천을 따라 산책하기 좋습니다.")
                .thumbnailUrl("https://example.com/cheonggyecheon.jpg")
                .category("자연경관")
                .build(),
            TouristSpot.builder()
                .id(6L)
                .name("롯데월드타워")
                .latitude(37.512558)
                .longitude(127.102545)
                .address("서울 송파구 올림픽로 300")
                .description("한국 최고 높이의 마천루로, 전망대에서 서울의 파노라마 뷰를 즐길 수 있습니다.")
                .thumbnailUrl("https://example.com/lottetower.jpg")
                .category("관광명소")
                .build(),
            TouristSpot.builder()
                .id(7L)
                .name("코엑스")
                .latitude(37.513322)
                .longitude(127.058716)
                .address("서울 강남구 영동대로 513")
                .description("아시아 최대 규모의 복합 쇼핑몰로, 쇼핑, 식사, 엔터테인먼트를 한 곳에서 즐길 수 있습니다.")
                .thumbnailUrl("https://example.com/coex.jpg")
                .category("쇼핑")
                .build(),
            TouristSpot.builder()
                .id(8L)
                .name("명동성당")
                .latitude(37.563592)
                .longitude(126.986970)
                .address("서울 중구 명동길 74")
                .description("한국 최초의 서양식 고딕 건축물로, 종교적인 의미와 함께 건축미를 감상할 수 있습니다.")
                .thumbnailUrl("https://example.com/myeongdongcathedral.jpg")
                .category("역사문화")
                .build(),
            TouristSpot.builder()
                .id(9L)
                .name("광화문광장")
                .latitude(37.570022)
                .longitude(126.976837)
                .address("서울 종로구 세종대로 172")
                .description("서울의 중심 광장으로, 역사적인 행사와 문화 행사가 열리는 상징적인 장소입니다.")
                .thumbnailUrl("https://example.com/gwanghwamun.jpg")
                .category("공원")
                .build(),
            TouristSpot.builder()
                .id(10L)
                .name("남산공원")
                .latitude(37.551169)
                .longitude(126.988226)
                .address("서울 용산구 남산공원길 105")
                .description("서울의 대표적인 도심 공원으로, 조망 좋은 산책로와 휴식 공간을 제공합니다.")
                .thumbnailUrl("https://example.com/namsanpark.jpg")
                .category("자연경관")
                .build(),
            TouristSpot.builder()
                .id(11L)
                .name("한강공원")
                .latitude(37.517523)
                .longitude(126.938858)
                .address("서울 용산구 한강대로405길 25")
                .description("서울의 허파라고 불리는 곳으로, 자전거 라이딩, 피크닉, 야경 감상을 즐길 수 있습니다.")
                .thumbnailUrl("https://example.com/hangangpark.jpg")
                .category("자연경관")
                .build(),
            TouristSpot.builder()
                .id(12L)
                .name("동대문디자인플라자")
                .latitude(37.566311)
                .longitude(127.009445)
                .address("서울 중구 을지로 281")
                .description("디자인과 문화를 테마로 한 복합 문화 공간으로, 다양한 전시와 이벤트가 열립니다.")
                .thumbnailUrl("https://example.com/ddp.jpg")
                .category("문화시설")
                .build()
        );
        
        log.info("Tourist spots data initialization completed. {} spots loaded.", touristSpots.size());
    }

    public List<TouristSpotListResponse> getAllTouristSpots() {
        log.info("Fetching all tourist spots for list view");
        return touristSpots.stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());
    }

    public TouristSpotDetailResponse getTouristSpotById(Long id) {
        log.info("Fetching tourist spot by id: {}", id);
        TouristSpot spot = touristSpots.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new TouristSpotNotFoundException("Tourist spot not found with id: " + id));
        return toDetailResponse(spot);
    }

    public List<TouristSpotMapResponse> getTouristSpotsForMap() {
        log.info("Fetching all tourist spots for map view");
        return touristSpots.stream()
                .map(this::toMapResponse)
                .collect(Collectors.toList());
    }

    public List<TouristSpotSearchResponse> searchTouristSpots(String keyword) {
        log.info("Searching tourist spots with keyword: {}", keyword);
        return touristSpots.stream()
                .filter(t -> t.getName().contains(keyword))
                .map(this::toSearchResponse)
                .collect(Collectors.toList());
    }

    private TouristSpotListResponse toListResponse(TouristSpot spot) {
        return TouristSpotListResponse.builder()
                .id(spot.getId())
                .name(spot.getName())
                .latitude(spot.getLatitude())
                .longitude(spot.getLongitude())
                .address(spot.getAddress())
                .description(spot.getDescription())
                .thumbnailUrl(spot.getThumbnailUrl())
                .category(spot.getCategory())
                .build();
    }

    private TouristSpotDetailResponse toDetailResponse(TouristSpot spot) {
        return TouristSpotDetailResponse.builder()
                .id(spot.getId())
                .name(spot.getName())
                .latitude(spot.getLatitude())
                .longitude(spot.getLongitude())
                .address(spot.getAddress())
                .description(spot.getDescription())
                .thumbnailUrl(spot.getThumbnailUrl())
                .category(spot.getCategory())
                .build();
    }

    private TouristSpotMapResponse toMapResponse(TouristSpot spot) {
        return TouristSpotMapResponse.builder()
                .id(spot.getId())
                .name(spot.getName())
                .latitude(spot.getLatitude())
                .longitude(spot.getLongitude())
                .build();
    }

    private TouristSpotSearchResponse toSearchResponse(TouristSpot spot) {
        return TouristSpotSearchResponse.builder()
                .id(spot.getId())
                .name(spot.getName())
                .build();
    }
}
