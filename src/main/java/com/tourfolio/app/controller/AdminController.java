package com.tourfolio.app.controller;

import com.tourfolio.app.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final StockService stockService;

    @PostMapping("/calculate")
    public String forceCalculate() {
        stockService.updateDailyStockPrices();
        return "SUCCESS: 모든 종목의 주가가 계산되었습니다.";
    }
}