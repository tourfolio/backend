package com.tourfolio.app.controller;

import com.tourfolio.app.exception.CustomException;
import com.tourfolio.app.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "관리", description = "정산 배치 수동 실행 및 시연 데이터 준비 (관리자 키 필요)")
public class AdminController {

    private final StockService stockService;

    @Value("${admin.api-key}")
    private String adminApiKey;

    @PostMapping("/calculate")
    @Operation(summary = "어드민 주가 정산 실행",
            description = "관광 지표(P/D/R/S)를 수집하여 전 종목의 주가를 정산합니다. 헤더에 X-Admin-Key가 필요합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "정산 배치 실행 성공"),
            @ApiResponse(responseCode = "401", description = "관리자 키 누락 또는 불일치"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public String forceCalculate(
            @Parameter(description = "관리자 인증 키") @RequestHeader(value = "X-Admin-Key", required = false) String adminKey) {
        if (adminKey == null || !adminKey.equals(adminApiKey)) {
            log.warn("⚠️ 관리자 배치 무단 접근 시도 차단");
            throw new CustomException("UNAUTHORIZED_ADMIN", "관리자 권한이 필요합니다.");
        }
        stockService.updateDailyStockPrices();
        return "SUCCESS: 모든 종목의 주가가 계산되었습니다.";
    }

}