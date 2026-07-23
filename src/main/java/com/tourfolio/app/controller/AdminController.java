package com.tourfolio.app.controller;

import com.tourfolio.app.service.PriceHistoryBackfillService;
import com.tourfolio.app.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "관리", description = "정산 배치 수동 실행 및 시연 데이터 준비")
public class AdminController {

    private final StockService stockService;
    private final PriceHistoryBackfillService priceHistoryBackfillService;

    @PostMapping("/calculate")
    @Operation(summary = "주가 정산 배치 수동 실행",
            description = "관광 지표(P/D/R/S)를 수집하여 전 종목의 주가를 정산합니다.")
    public String forceCalculate() {
        stockService.updateDailyStockPrices();
        return "SUCCESS: 모든 종목의 주가가 계산되었습니다.";
    }

    @PostMapping("/backfill")
    @Operation(summary = "차트용 과거 시세 백필",
            description = "차트 시연을 위해 과거 N일치 시세 이력을 생성합니다. "
                    + "가우시안 랜덤워크 기반 합성 데이터이며, 이미 존재하는 날짜는 덮어쓰지 않습니다.")
    public String backfill(@RequestParam(defaultValue = "30") int days) {
        int created = priceHistoryBackfillService.backfill(days);
        return "SUCCESS: 최근 " + days + "일 시세 이력 " + created + "행을 생성했습니다.";
    }
}
