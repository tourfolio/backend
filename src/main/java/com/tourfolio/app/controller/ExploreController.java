// src/main/java/com/tourfolio/app/controller/ExploreController.java
package com.tourfolio.app.controller;

import com.tourfolio.app.dto.ExploreResponse;
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
@Tag(name = "탐색(Explore)", description = "관광지 탐색 화면용 복합 필터링 API")
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

    @GetMapping("/search")
    @Operation(
            summary = "탐색 화면 복합 필터링 검색",
            description = "키워드, 지역 코드, 테마 태그를 조합하여 관광지를 검색합니다. 모든 파라미터는 선택적이며, null이거나 공백인 경우 해당 조건은 무시됩니다. 세 가지 조건이 동시에 적용될 수 있습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "필터링 검색 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 파라미터"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<List<ExploreResponse>> searchExploreCards(
            @Parameter(
                    description = "관광지명 검색 키워드 (대소문자 구분 없이 부분 일치 검색)",
                    example = "해운대",
                    required = false
            )
            @RequestParam(required = false) String keyword,
            
            @Parameter(
                    description = "지역 코드 (예: 1=서울, 6=부산, 39=제주 등)",
                    example = "6",
                    required = false
            )
            @RequestParam(required = false) String areaCode,
            
            @Parameter(
                    description = "테마 태그 (예: 역사, 자연, 레저, 문화 등)",
                    example = "레저",
                    required = false
            )
            @RequestParam(required = false) String themeTag) {
        
        log.info("GET /api/v1/explore/search - 탐색 화면 복합 필터링 검색 요청: keyword={}, areaCode={}, themeTag={}", 
                keyword, areaCode, themeTag);
        
        List<ExploreResponse> cards = exploreService.searchExploreCards(keyword, areaCode, themeTag);
        return ResponseEntity.ok(cards);
    }
}
