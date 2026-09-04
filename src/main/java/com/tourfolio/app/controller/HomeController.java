package com.tourfolio.app.controller;

import com.tourfolio.app.dto.HomeResponse;
import com.tourfolio.app.security.SecurityUtil;
import com.tourfolio.app.service.HomeService;
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
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
@Tag(name = "홈", description = "홈 화면 통합 조회 API")
public class HomeController {

    private final HomeService homeService;

    @GetMapping
    @Operation(summary = "홈 화면 조회", description = "내 포트폴리오, 보유 카드 현황, 이번주 추천 관광지를 한 번에 조회합니다.(토큰 필요)")
    public ResponseEntity<HomeResponse> getHome() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("GET /api/v1/home - 홈 화면 조회: userId={}", userId);
        return ResponseEntity.ok(homeService.getHome(userId));
    }
}