package com.tourfolio.app.controller;

import com.tourfolio.app.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @PostMapping("/calculate")
    @Operation(summary = "주가 정산 배치 수동 실행",
            description = "관광 지표(P/D/R/S)를 수집하여 전 종목의 주가를 정산합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정산 배치 실행 성공"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public String forceCalculate() {
        stockService.updateDailyStockPrices();
        return "SUCCESS: 모든 종목의 주가가 계산되었습니다.";
    }

}
