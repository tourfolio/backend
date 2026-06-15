package com.tourfolio.app.controller;

import com.tourfolio.app.dto.StockResponse;
import com.tourfolio.app.dto.TradeRequest;
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
        log.info("GET /api/v1/stocks - Fetching all stocks");
        List<StockResponse> stocks = stockService.getAllStocks();
        return ResponseEntity.ok(stocks);
    }

    @GetMapping("/test-api")
    public ResponseEntity<Map<String, Object>> testOpenApiMetrics(@RequestParam Long spotId) {
        log.info("GET /api/v1/stocks/test-api - Testing Open API metrics for spotId: {}", spotId);
        try {
            Map<String, Object> metrics = stockService.testOpenApiMetrics(spotId);
            return ResponseEntity.ok(metrics);
        } catch (IllegalArgumentException e) {
            log.error("Test API metrics failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error during test API metrics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/trade")
    public ResponseEntity<Transaction> executeTrade(@RequestBody TradeRequest request) {
        log.info("POST /api/v1/stocks/trade - Executing trade: {}", request);
        try {
            Transaction transaction = stockService.executeTrade(request);
            return ResponseEntity.ok(transaction);
        } catch (IllegalArgumentException e) {
            log.error("Trade execution failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error during trade execution: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
