package com.tourfolio.app.controller;

import com.tourfolio.app.dto.AcquireCardRequest;
import com.tourfolio.app.dto.CardDetailResponse;
import com.tourfolio.app.dto.CollectionResponse;
import com.tourfolio.app.entity.Card;
import com.tourfolio.app.service.CollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/collection")
@RequiredArgsConstructor
@Tag(name = "수집(Collection)", description = "포토카드 수집 및 GPS 기반 방문 인증 API")
public class CollectionController {

    private final CollectionService collectionService;

    @GetMapping
    @Operation(
            summary = "수집 메인 화면 조회",
            description = "수집 탭 메인 화면의 카드 목록을 조회합니다. 지역, 테마, 희귀도 필터링을 지원하며, 필터 적용 시 해당 범위 내 수집률을 계산합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수집 메인 화면 조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 파라미터"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<CollectionResponse> getCollection(
            @Parameter(description = "유저 ID", example = "1", required = true)
            @RequestParam Long userId,

            @Parameter(description = "지역 필터 (예: 서울, 부산, 제주)", example = "서울", required = false)
            @RequestParam(required = false) String region,

            @Parameter(description = "테마 필터 (예: 역사, 자연, 레저, 문화)", example = "역사", required = false)
            @RequestParam(required = false) String theme,

            @Parameter(description = "희귀도 필터 (LEGEND, EPIC, RARE, NORMAL)", example = "EPIC", required = false)
            @RequestParam(required = false) Card.CardRarity rarity) {

        log.info("GET /api/v1/collection - 수집 메인 화면 조회 요청: userId={}, region={}, theme={}, rarity={}", 
                userId, region, theme, rarity);

        CollectionResponse response = collectionService.getCollection(userId, region, theme, rarity);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cards/{cardId}")
    @Operation(
            summary = "포토카드 상세 조회",
            description = "특정 포토카드의 상세 정보를 조회합니다. 보유 여부에 따라 앞면/뒷면 데이터를 다르게 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "포토카드 상세 조회 성공"),
            @ApiResponse(responseCode = "404", description = "카드 또는 관광지를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<CardDetailResponse> getCardDetail(
            @Parameter(description = "유저 ID", example = "1", required = true)
            @RequestParam Long userId,

            @Parameter(description = "카드 ID", example = "1", required = true)
            @PathVariable Long cardId) {

        log.info("GET /api/v1/collection/cards/{} - 포토카드 상세 조회 요청: userId={}", cardId, userId);

        CardDetailResponse response = collectionService.getCardDetail(userId, cardId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cards/{cardId}/acquire")
    @Operation(
            summary = "GPS 기반 카드 획득 (방문 인증)",
            description = "현재 GPS 좌표를 기반으로 관광지 방문을 인증하고 포토카드를 획득합니다. 관광지 반경 내에 있어야 카드를 획득할 수 있습니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카드 획득 성공"),
            @ApiResponse(responseCode = "400", description = "이미 보유한 카드이거나 위치가 너무 멈"),
            @ApiResponse(responseCode = "404", description = "카드를 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<Void> acquireCard(
            @Parameter(description = "유저 ID", example = "1", required = true)
            @RequestParam Long userId,

            @Parameter(description = "카드 ID", example = "1", required = true)
            @PathVariable Long cardId,

            @Valid @RequestBody AcquireCardRequest request) {

        log.info("POST /api/v1/collection/cards/{}/acquire - GPS 기반 카드 획득 요청: userId={}, distanceInMeters={}",
                cardId, userId, request.getDistanceInMeters());

        collectionService.acquireCard(userId, cardId, request);
        return ResponseEntity.ok().build();
    }
}
