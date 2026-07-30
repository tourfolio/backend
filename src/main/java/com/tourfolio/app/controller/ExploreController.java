// src/main/java/com/tourfolio/app/controller/ExploreController.java
package com.tourfolio.app.controller;

import com.tourfolio.app.dto.*;
import com.tourfolio.app.service.ExploreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/v1/explore")
@RequiredArgsConstructor
@Tag(name = "탐색(Explore)", description = "관광지 탐색 화면용 API")
public class ExploreController {

    private final ExploreService exploreService;

    @GetMapping("/cards")
    @Operation(
            summary = "탐색 화면 전체 카드 조회",
            description = "화면 진입 시 모든 관광지 카드를 티어 오름차순, 이름 오름차순으로 반환합니다. 필터링 없이 전체 목록을 보여줍니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "전체 카드 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<List<ExploreResponse>> getAllExploreCards() {
        log.info("GET /api/v1/explore/cards - 탐색 화면 전체 카드 조회 요청");
        List<ExploreResponse> cards = exploreService.getAllExploreCards();
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/main-cards")
    @Operation(
            summary = "풀스크린 메인 카드 조회 (Editor's Pick)",
            description = "탐색 탭 첫 진입 시 보이는 메인 풀스크린 슬라이드 카드 목록을 조회합니다. Editor's Pick 등 메인 큐레이션 관광지 5~6개를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메인 카드 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<List<MainCardResponse>> getMainCards() {
        log.info("GET /api/v1/explore/main-cards - 풀스크린 메인 카드 조회 요청");
        List<MainCardResponse> cards = exploreService.getMainCards();
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/hub")
    @Operation(
            summary = "컨텐츠 허브 조회",
            description = "메인에서 검색 아이콘을 눌렀을 때 이동하는 '컨텐츠 허브' 화면 데이터를 조회합니다. 테마 목록과 인기/급상승 관광지를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "컨텐츠 허브 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<HubResponse> getHubData() {
        log.info("GET /api/v1/explore/hub - 컨텐츠 허브 조회 요청");
        HubResponse hubData = exploreService.getHubData();
        return ResponseEntity.ok(hubData);
    }

    @GetMapping("/themes/{themeId}/spots")
    @Operation(
            summary = "테마별 관광지 목록 조회",
            description = "컨텐츠 허브에서 특정 테마 카드를 클릭했을 때 해당 테마에 속한 관광지 리스트를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "테마별 관광지 목록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "테마를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<ThemeDetailResponse> getThemeDetail(
            @Parameter(description = "테마 ID", example = "1", required = true)
            @PathVariable("themeId") String themeId) {
        log.info("GET /api/v1/explore/themes/{}/spots - 테마별 관광지 목록 조회 요청", themeId);
        ThemeDetailResponse themeDetail = exploreService.getThemeDetail(themeId);
        return ResponseEntity.ok(themeDetail);
    }

    @GetMapping("/spots/{spotId}")
    @Operation(
            summary = "관광지 상세 정보 조회",
            description = "탐색 탭 내 관광지 상세 페이지를 조회합니다. 기본 정보, 매력 포인트, 주변 관광지를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "관광지 상세 정보 조회 성공"),
            @ApiResponse(responseCode = "404", description = "관광지를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<SpotDetailResponse> getSpotDetail(
            @Parameter(description = "관광지 ID", example = "1", required = true)
            @PathVariable("spotId") Long spotId) {
        log.info("GET /api/v1/explore/spots/{} - 관광지 상세 정보 조회 요청", spotId);
        SpotDetailResponse spotDetail = exploreService.getSpotDetail(spotId);
        return ResponseEntity.ok(spotDetail);
    }

    @GetMapping("/search")
    @Operation(
            summary = "탐색 화면 복합 필터링 검색",
            description = "키워드, 지역(다중 선택), 테마(다중 선택)를 조합하여 관광지를 검색합니다. 키워드는 관광지명과 태그명 모두 검색합니다. 모든 파라미터는 선택적이며, null이거나 비어있는 경우 해당 조건은 무시됩니다. 검색된 관광지 목록과 총 개수를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "필터링 검색 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 파라미터"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<SearchResponse> searchExploreCards(
            @Parameter(
                    description = "검색어 (관광지명, 태그명 검색)",
                    example = "해운대",
                    required = false
            )
            @RequestParam(required = false) String keyword,

            @Parameter(
                    description = "지역 필터 (다중 선택, 예: 서울, 부산, 제주)",
                    example = "서울,부산",
                    required = false
            )
            @RequestParam(required = false) List<String> regions,

            @Parameter(
                    description = "테마 필터 (다중 선택, 예: 역사, 문화, 자연)",
                    example = "역사,문화",
                    required = false
            )
            @RequestParam(required = false) List<String> themes) {

        log.info("GET /api/v1/explore/search - 탐색 화면 복합 필터링 검색 요청: keyword={}, regions={}, themes={}",
                keyword, regions, themes);

        SearchResponse searchResult = exploreService.searchSpotsWithTotalCount(keyword, regions, themes);
        return ResponseEntity.ok(searchResult);
    }
}
