// src/main/java/com/tourfolio/app/controller/StockController.java
package com.tourfolio.app.controller;

import com.tourfolio.app.dto.StockResponse;
import com.tourfolio.app.dto.TradeRequest;
import com.tourfolio.app.dto.MemberAssetResponse;
import com.tourfolio.app.dto.PriceHistoryResponse;
import com.tourfolio.app.dto.RegionalIndexResponse;
import com.tourfolio.app.dto.StockChartResponse;
import com.tourfolio.app.dto.PortfolioSummaryResponse;
import com.tourfolio.app.entity.Transaction;
import com.tourfolio.app.service.StockService;
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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "주식/포트폴리오", description = "관광지 기반 가상 주식, 시세, 차트, 거래, 포트폴리오 API")
public class StockController {

    private final StockService stockService;

    @GetMapping("/stocks")
    @Operation(
            summary = "주식 목록 조회 (통합)",
            description = "지역, 키워드, 태그(다중 선택), 정렬 기준으로 관광 주식을 검색합니다. 모든 파라미터는 선택적이며, 기본값으로 전체 목록을 등락률 내림차순으로 반환합니다."
    )
    public ResponseEntity<List<StockResponse>> getAllStocks(
            @Parameter(
                    description = "지역 필터 (예: '11', '26' 또는 'ALL'/'전체' 수신 시 전체 조회)",
                    example = "ALL",
                    required = false
            )
            @RequestParam(required = false, defaultValue = "ALL") String region,

            @Parameter(
                    description = "검색어 (주식/관광지명, 지역명 검색)",
                    example = "해운대",
                    required = false
            )
            @RequestParam(required = false) String keyword,

            @Parameter(
                    description = "태그 필터 (다중 선택, 예: 자연,문화,역사)",
                    example = "자연,문화",
                    required = false
            )
            @RequestParam(required = false) List<String> tags,

            @Parameter(
                    description = "정렬 기준 (price, changeRate, name, tier)",
                    example = "changeRate",
                    required = false
            )
            @RequestParam(required = false, defaultValue = "changeRate") String sortBy,

            @Parameter(
                    description = "정렬 방향 (ASC, DESC)",
                    example = "DESC",
                    required = false
            )
            @RequestParam(required = false, defaultValue = "DESC") String sortOrder) {
        log.info("GET /api/stocks - 주식 목록 통합 조회: region={}, keyword={}, tags={}, sortBy={}, sortOrder={}", region, keyword, tags, sortBy, sortOrder);
        return ResponseEntity.ok(stockService.searchStocksUnified(region, keyword, tags, sortBy, sortOrder));
    }

    @GetMapping("/stocks/top-gainers")
    @Operation(summary = "급등 주식 TOP 3 조회", description = "등락률이 높은 주식 상위 3개를 조회합니다.")
    public ResponseEntity<List<StockResponse>> getTopGainers() {
        log.info("GET /api/stocks/top-gainers - 급등 TOP 3 조회");
        return ResponseEntity.ok(stockService.getTopGainers());
    }

    @GetMapping("/stocks/top-losers")
    @Operation(summary = "급락 주식 TOP 3 조회", description = "등락률이 낮은 주식 하위 3개를 조회합니다.")
    public ResponseEntity<List<StockResponse>> getTopLosers() {
        log.info("GET /api/stocks/top-losers - 급락 TOP 3 조회");
        return ResponseEntity.ok(stockService.getTopLosers());
    }

    @GetMapping("/stocks/trending")
    @Operation(summary = "지금 뜨는 여행지 조회", description = "등락률 기준 상위 10개 인기 여행지 주식을 조회합니다.")
    public ResponseEntity<List<StockResponse>> getTrendingStocks() {
        log.info("GET /api/stocks/trending - 지금 뜨는 여행지 조회");
        return ResponseEntity.ok(stockService.getTrendingStocks());
    }

    @GetMapping("/stocks/regional-index")
    @Operation(summary = "지역별 지수 조회", description = "지역별 관광 주식의 평균 등락률과 종목 수를 조회합니다.")
    public ResponseEntity<List<RegionalIndexResponse>> getRegionalIndex() {
        log.info("GET /api/stocks/regional-index - 지역별 주요 지수 조회");
        return ResponseEntity.ok(stockService.getRegionalIndex());
    }

    @GetMapping("/price-history/{spotId}")
    @Operation(summary = "주식 가격 이력 조회", description = "특정 관광 주식의 기간별 가격 차트 데이터를 조회합니다.")
    public ResponseEntity<List<PriceHistoryResponse>> getPriceHistory(
            @PathVariable Long spotId,
            @RequestParam(required = false, defaultValue = "1w") String period) {
        log.info("GET /api/price-history/{} - 차트 데이터 조회: period={}", spotId, period);
        return ResponseEntity.ok(stockService.getPriceHistory(spotId, period));
    }

    @GetMapping("/stocks/{spotId}/chart")
    @Operation(summary = "주식 차트 데이터 조회", description = "실제 DB price_history 테이블 기반 기간별 차트 데이터를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "차트 데이터 조회 성공"),
            @ApiResponse(responseCode = "404", description = "주식 종목을 찾을 수 없음")
    })
    public ResponseEntity<List<StockChartResponse>> getStockChart(
            @PathVariable Long spotId,
            @Parameter(description = "기간 (1W, 3M, 1Y, 5Y, ALL)", example = "1W", required = false)
            @RequestParam(required = false, defaultValue = "1W") String period) {
        log.info("GET /api/stocks/{}/chart - 차트 데이터 조회: period={}", spotId, period);
        return ResponseEntity.ok(stockService.getStockChart(spotId, period));
    }

    @PostMapping("/stocks/trade")
    @Operation(summary = "가상 주식 매수/매도", description = "회원 ID, 종목 ID, 거래 유형, 수량을 받아 가상 주식 거래를 체결합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "거래 성공"),
            @ApiResponse(responseCode = "400", description = "잔액 부족 또는 유효하지 않은 요청"),
            @ApiResponse(responseCode = "404", description = "주식 종목을 찾을 수 없음")
    })
    public ResponseEntity<Transaction> executeTrade(@Valid @RequestBody TradeRequest request) {
        log.info("POST /api/stocks/trade - 가상 체결 시스템 오더 수신: {}", request);
        Transaction tx = stockService.executeTrade(request);
        return ResponseEntity.ok(tx);
    }

    @GetMapping("/portfolio")
    @Operation(summary = "회원 포트폴리오 조회", description = "회원의 현금, 보유 주식 평가금액, 수익률, 보유 종목 목록을 조회합니다.")
    public ResponseEntity<MemberAssetResponse> getMemberAssets(
            @RequestParam Long memberId,
            @RequestParam(required = false, defaultValue = "profit_rate") String sort) {
        log.info("GET /api/portfolio - 유저 포트폴리오 자산 스크리너 조회 ID: {}, sort={}", memberId, sort);
        return ResponseEntity.ok(stockService.getMemberAssets(memberId, sort));
    }

    @GetMapping("/portfolio/summary")
    @Operation(summary = "포트폴리오 요약 조회", description = "투자 홈 메인용 총 평가금액, 평가손익, 수익률, 자산 추이 데이터를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "포트폴리오 요약 조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public ResponseEntity<PortfolioSummaryResponse> getPortfolioSummary(
            @Parameter(description = "사용자 ID", required = true)
            @RequestParam Long userId,
            @Parameter(description = "자산 추이 기간 (1W, 1M, 3M, 1Y, ALL)", example = "1W", required = false)
            @RequestParam(required = false, defaultValue = "1W") String period) {
        log.info("GET /api/portfolio/summary - 포트폴리오 요약 조회: userId={}, period={}", userId, period);
        return ResponseEntity.ok(stockService.getPortfolioSummary(userId, period));
    }

    @PostMapping("/stocks/update-prices")
    @Operation(summary = "수동 주가 업데이트 (개발/검수용)", description = "스케줄러 주기와 상관없이 주가를 즉시 갱신합니다. 개발 및 검수용으로 사용하세요.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "주가 업데이트 성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<Void> updatePricesManually() {
        log.info("POST /api/stocks/update-prices - 수동 주가 업데이트 트리거");
        stockService.updateDailyStockPrices();
        return ResponseEntity.ok().build();
    }
}
