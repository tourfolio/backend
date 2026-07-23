// src/main/java/com/tourfolio/app/controller/StockController.java
package com.tourfolio.app.controller;

import com.tourfolio.app.dto.StockResponse;
import com.tourfolio.app.dto.TradeRequest;
import com.tourfolio.app.dto.MemberAssetResponse;
import com.tourfolio.app.dto.PriceHistoryResponse;
import com.tourfolio.app.dto.RegionalIndexResponse;
import com.tourfolio.app.entity.Transaction;
import com.tourfolio.app.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    @Operation(summary = "전체 주식 목록 조회", description = "관광지 기반 가상 주식 전체 목록과 현재가, 등락률을 조회합니다.")
    public ResponseEntity<List<StockResponse>> getAllStocks() {
        log.info("GET /api/stocks - 전광판 시세조회판 트리거 호출");
        return ResponseEntity.ok(stockService.getAllStocks());
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

    @GetMapping("/stocks/regional-index")
    @Operation(summary = "지역별 지수 조회", description = "지역별 관광 주식의 평균 등락률과 종목 수를 조회합니다.")
    public ResponseEntity<List<RegionalIndexResponse>> getRegionalIndex() {
        log.info("GET /api/stocks/regional-index - 지역별 주요 지수 조회");
        return ResponseEntity.ok(stockService.getRegionalIndex());
    }

    @GetMapping("/stocks/search")
    @Operation(summary = "주식 검색 및 정렬", description = "지역, 테마, 정렬 기준으로 관광 주식을 검색합니다.")
    public ResponseEntity<List<StockResponse>> searchStocks(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String theme,
            @RequestParam(required = false, defaultValue = "change_rate") String sort) {
        log.info("GET /api/stocks/search - 종목 탐색: region={}, theme={}, sort={}", region, theme, sort);
        return ResponseEntity.ok(stockService.searchStocks(region, theme, sort));
    }

    @GetMapping("/price-history/{spotId}")
    @Operation(summary = "주식 가격 이력 조회", description = "특정 관광 주식의 기간별 가격 차트 데이터를 조회합니다.")
    public ResponseEntity<List<PriceHistoryResponse>> getPriceHistory(
            @PathVariable Long spotId,
            @RequestParam(required = false, defaultValue = "1w") String period) {
        log.info("GET /api/price-history/{} - 차트 데이터 조회: period={}", spotId, period);
        return ResponseEntity.ok(stockService.getPriceHistory(spotId, period));
    }

    @PostMapping("/stocks/trade")
    @Operation(summary = "가상 주식 매수/매도", description = "회원 ID, 종목 ID, 거래 유형, 수량을 받아 가상 주식 거래를 체결합니다.")
    public ResponseEntity<Transaction> executeTrade(@RequestBody TradeRequest request) {
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
}
