package com.tourfolio.app.controller;

import com.tourfolio.app.dto.TouristSpotDetailResponse;
import com.tourfolio.app.dto.TouristSpotListResponse;
import com.tourfolio.app.dto.TouristSpotMapResponse;
import com.tourfolio.app.dto.TouristSpotSearchResponse;
import com.tourfolio.app.service.TouristSpotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tourist-spots")
@RequiredArgsConstructor
@Tag(name = "Tourist Spots", description = "관광지 정보 API")
public class TouristSpotController {

    private final TouristSpotService touristSpotService;

    @GetMapping
    @Operation(summary = "관광지 목록 조회", description = "모든 관광지의 기본 정보를 반환합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공", 
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = TouristSpotListResponse.class),
                examples = @ExampleObject(
                    name = "관광지 목록 예시",
                    value = "[{\"id\":1,\"name\":\"경복궁\",\"latitude\":37.579617,\"longitude\":126.977041,\"address\":\"서울 종로구 사직로 161\",\"description\":\"조선시대 대표 궁궐로, 한국의 전통 건축미를 감상할 수 있는 역사적인 장소입니다.\",\"thumbnailUrl\":\"https://example.com/gyeongbokgung.jpg\",\"category\":\"역사문화\"}]"
                )))
    })
    public ResponseEntity<List<TouristSpotListResponse>> getAllTouristSpots() {
        log.info("GET /api/tourist-spots - Fetching all tourist spots");
        List<TouristSpotListResponse> spots = touristSpotService.getAllTouristSpots();
        return ResponseEntity.ok(spots);
    }

    @GetMapping("/{id}")
    @Operation(summary = "관광지 상세 조회", description = "특정 관광지의 상세 정보를 반환합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공", 
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = TouristSpotDetailResponse.class),
                examples = @ExampleObject(
                    name = "관광지 상세 예시",
                    value = "{\"id\":1,\"name\":\"경복궁\",\"latitude\":37.579617,\"longitude\":126.977041,\"address\":\"서울 종로구 사직로 161\",\"description\":\"조선시대 대표 궁궐로, 한국의 전통 건축미를 감상할 수 있는 역사적인 장소입니다.\",\"thumbnailUrl\":\"https://example.com/gyeongbokgung.jpg\",\"category\":\"역사문화\"}"
                ))),
        @ApiResponse(responseCode = "404", description = "관광지를 찾을 수 없음")
    })
    public ResponseEntity<TouristSpotDetailResponse> getTouristSpotById(
            @Parameter(description = "관광지 ID", required = true, example = "1")
            @PathVariable Long id) {
        log.info("GET /api/tourist-spots/{} - Fetching tourist spot detail", id);
        TouristSpotDetailResponse spot = touristSpotService.getTouristSpotById(id);
        return ResponseEntity.ok(spot);
    }

    @GetMapping("/map")
    @Operation(summary = "지도 마커 조회", description = "지도 표시에 필요한 관광지의 위치 정보만 반환합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공", 
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = TouristSpotMapResponse.class),
                examples = @ExampleObject(
                    name = "지도 마커 예시",
                    value = "[{\"id\":1,\"name\":\"경복궁\",\"latitude\":37.579617,\"longitude\":126.977041}]"
                )))
    })
    public ResponseEntity<List<TouristSpotMapResponse>> getTouristSpotsForMap() {
        log.info("GET /api/tourist-spots/map - Fetching tourist spots for map");
        List<TouristSpotMapResponse> spots = touristSpotService.getTouristSpotsForMap();
        return ResponseEntity.ok(spots);
    }

    @GetMapping("/search")
    @Operation(summary = "관광지 검색", description = "키워드로 관광지를 검색합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "검색 성공", 
            content = @Content(mediaType = "application/json", 
                schema = @Schema(implementation = TouristSpotSearchResponse.class),
                examples = @ExampleObject(
                    name = "검색 결과 예시",
                    value = "[{\"id\":1,\"name\":\"경복궁\"},{\"id\":2,\"name\":\"창덕궁\"}]"
                )))
    })
    public ResponseEntity<List<TouristSpotSearchResponse>> searchTouristSpots(
            @Parameter(description = "검색 키워드", required = true, example = "궁")
            @RequestParam String keyword) {
        log.info("GET /api/tourist-spots/search?keyword={} - Searching tourist spots", keyword);
        List<TouristSpotSearchResponse> spots = touristSpotService.searchTouristSpots(keyword);
        return ResponseEntity.ok(spots);
    }
}
