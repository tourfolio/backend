package com.tourfolio.app.controller;

import com.tourfolio.app.dto.TouristSpot;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Tourist Spots", description = "관광지 정보 API")
public class TouristSpotController {

    @GetMapping("/tourist-spots")
    @Operation(summary = "전체 관광지 목록 조회", description = "12개의 주요 관광지 정보를 반환합니다.")
    public ResponseEntity<List<TouristSpot>> getAllTouristSpots() {
        List<TouristSpot> touristSpots = Arrays.asList(
            TouristSpot.builder()
                .id(1L)
                .name("경복궁")
                .latitude(37.579617)
                .longitude(126.977041)
                .build(),
            TouristSpot.builder()
                .id(2L)
                .name("남산타워")
                .latitude(37.551169)
                .longitude(126.988226)
                .build(),
            TouristSpot.builder()
                .id(3L)
                .name("롯데월드")
                .latitude(37.5115)
                .longitude(127.0980)
                .build(),
            TouristSpot.builder()
                .id(4L)
                .name("해운대해수욕장")
                .latitude(35.1586)
                .longitude(129.1604)
                .build(),
            TouristSpot.builder()
                .id(5L)
                .name("감천문화마을")
                .latitude(35.0942)
                .longitude(128.9938)
                .build(),
            TouristSpot.builder()
                .id(6L)
                .name("불국사")
                .latitude(35.7899)
                .longitude(129.1119)
                .build(),
            TouristSpot.builder()
                .id(7L)
                .name("첨성대")
                .latitude(35.8358)
                .longitude(129.2115)
                .build(),
            TouristSpot.builder()
                .id(8L)
                .name("전주한옥마을")
                .latitude(35.8142)
                .longitude(127.1536)
                .build(),
            TouristSpot.builder()
                .id(9L)
                .name("여수해상케이블카")
                .latitude(34.7466)
                .longitude(127.7358)
                .build(),
            TouristSpot.builder()
                .id(10L)
                .name("성산일출봉")
                .latitude(33.4565)
                .longitude(126.9426)
                .build(),
            TouristSpot.builder()
                .id(11L)
                .name("만장굴")
                .latitude(33.5286)
                .longitude(126.7722)
                .build(),
            TouristSpot.builder()
                .id(12L)
                .name("설악산")
                .latitude(38.1195)
                .longitude(128.0560)
                .build()
        );
        
        return ResponseEntity.ok(touristSpots);
    }
}
