package com.tourfolio.app.controller;

import com.tourfolio.app.dto.PointHistoryResponse;
import com.tourfolio.app.security.SecurityUtil;
import com.tourfolio.app.service.PointHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/points")
@RequiredArgsConstructor
@Tag(name = "포인트 내역", description = "포인트(잔액) 변동 통합 내역 조회 API")
public class PointHistoryController {

    private final PointHistoryService pointHistoryService;

    @GetMapping("/history")
    @Operation(summary = "포인트 내역 조회", description = "출석/미션 보상, 주식 거래를 포함한 통합 잔액 변동 내역을 조회합니다.")
    public ResponseEntity<PointHistoryResponse> getHistory() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("GET /api/v1/points/history - 포인트 내역 조회: userId={}", userId);
        return ResponseEntity.ok(pointHistoryService.getHistory(userId));
    }
}