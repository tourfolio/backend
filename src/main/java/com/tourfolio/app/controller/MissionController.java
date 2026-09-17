package com.tourfolio.app.controller;

import com.tourfolio.app.dto.MissionClaimResponse;
import com.tourfolio.app.dto.MissionListResponse;
import com.tourfolio.app.security.SecurityUtil;
import com.tourfolio.app.service.MissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/missions")
@RequiredArgsConstructor
@Tag(name = "미션/업적", description = "미션 진행 현황 및 포인트 관련 API")
public class MissionController {

    private final MissionService missionService;

    @GetMapping
    @Operation(summary = "업적 메인 화면 조회", description = "보유 포인트, 이번주 출석, 진행중/완료 미션 목록을 조회합니다. (토큰 필요)")
    public ResponseEntity<MissionListResponse> getMissions() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("GET /api/v1/missions - 업적 메인 화면 조회: userId={}", userId);
        return ResponseEntity.ok(missionService.getMissions(userId));
    }

    @PostMapping("/{missionId}/claim")
    @Operation(summary = "수집 미션 보상 수령", description = "프론트에서 카드 보유 조건 충족을 판정한 COLLECT 미션의 완료 처리 및 포인트 지급을 요청합니다. (토큰 필요)")
    public ResponseEntity<MissionClaimResponse> claimCollectMission(@PathVariable Long missionId) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("POST /api/v1/missions/{}/claim - 수집 미션 보상 수령 요청: userId={}", missionId, userId);
        return ResponseEntity.ok(missionService.claimCollectMission(userId, missionId));
    }
}