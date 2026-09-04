package com.tourfolio.app.controller;

import com.tourfolio.app.dto.MyPageResponse;
import com.tourfolio.app.security.SecurityUtil;
import com.tourfolio.app.service.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
@Tag(name = "마이페이지", description = "마이페이지 요약 조회 및 회원 탈퇴 API")
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping
    @Operation(summary = "마이페이지 요약 조회", description = "닉네임, 보유 포인트, 보유 카드 수, 총 수익률을 조회합니다. (토큰 필요)")
    public ResponseEntity<MyPageResponse> getMyPage() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("GET /api/v1/mypage - 마이페이지 요약 조회: userId={}", userId);
        return ResponseEntity.ok(myPageService.getMyPage(userId));
    }

    @DeleteMapping
    @Operation(summary = "회원 탈퇴", description = "계정을 비활성화(소프트 삭제) 처리합니다. (토큰 필요)")
    public ResponseEntity<Void> withdraw() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("DELETE /api/v1/mypage - 회원 탈퇴 요청: userId={}", userId);
        myPageService.withdraw(userId);
        return ResponseEntity.ok().build();
    }
}