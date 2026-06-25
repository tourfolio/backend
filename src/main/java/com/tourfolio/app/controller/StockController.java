package com.tourfolio.app.controller;

import com.tourfolio.app.dto.StockResponse;
import com.tourfolio.app.dto.TradeRequest;
import com.tourfolio.app.dto.MemberAssetResponse;
import com.tourfolio.app.entity.Transaction;
import com.tourfolio.app.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping
    public ResponseEntity<List<StockResponse>> getAllStocks() {
        log.info("GET /api/v1/stocks - 전광판 시세조회판 트리거 호출");
        return ResponseEntity.ok(stockService.getAllStocks());
    }

    @PostMapping("/trade")
    public ResponseEntity<Transaction> executeTrade(@RequestBody TradeRequest request) {
        log.info("POST /api/v1/stocks/trade - 가상 체결 시스템 오더 수신: {}", request);
        Transaction tx = stockService.executeTrade(request);
        return ResponseEntity.ok(tx);
    }

    @GetMapping("/assets")
    public ResponseEntity<MemberAssetResponse> getMemberAssets(@RequestParam Long memberId) {
        log.info("GET /api/v1/stocks/assets - 유저 포트폴리오 자산 스크리너 조회 ID: {}", memberId);
        return ResponseEntity.ok(stockService.getMemberAssets(memberId));
    }

    @GetMapping("/test-api")
    public ResponseEntity<Map<String, Object>> testOpenApiMetrics(@RequestParam Long spotId) {
        return ResponseEntity.ok(stockService.testOpenApiMetrics(spotId));
    }
}