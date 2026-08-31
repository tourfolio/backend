// src/main/java/com/tourfolio/app/controller/UserController.java
package com.tourfolio.app.controller;

import com.tourfolio.app.dto.ChargeRequest;
import com.tourfolio.app.dto.ProfileUpdateRequest;
import com.tourfolio.app.entity.User;
import com.tourfolio.app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "사용자 (User)", description = "자산 충전 및 프로필 수정 API")
public class UserController {

    private final UserService userService;

    @PostMapping("/charge")
    @Operation(summary = "자산 충전", description = "회원의 예수금을 충전합니다. 테스트용 포인트 충전에 사용됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "자산 충전 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 충전 금액"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    public ResponseEntity<User> chargeBalance(@Valid @RequestBody ChargeRequest request) {
        Long memberId = com.tourfolio.app.security.SecurityUtil.getCurrentUserId();
        log.info("POST /api/v1/user/charge - 자산 충전 요청: memberId={}, amount={}",
                memberId, request.getAmount());
        User user = userService.chargeBalance(memberId, request.getAmount());
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    @Operation(summary = "프로필 수정", description = "회원의 닉네임을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
            @ApiResponse(responseCode = "400", description = "이미 사용 중인 닉네임 또는 유효하지 않은 요청"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    public ResponseEntity<User> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        Long memberId = com.tourfolio.app.security.SecurityUtil.getCurrentUserId();
        log.info("PUT /api/v1/user/profile - 프로필 수정 요청: memberId={}, newNickname={}",
                memberId, request.getNickname());
        User user = userService.updateProfile(memberId, request.getNickname());
        return ResponseEntity.ok(user);
    }
}