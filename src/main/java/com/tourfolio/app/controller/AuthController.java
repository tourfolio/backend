package com.tourfolio.app.controller;

import com.tourfolio.app.dto.AuthResponse;
import com.tourfolio.app.dto.KakaoAuthRequest;
import com.tourfolio.app.dto.LoginRequest;
import com.tourfolio.app.dto.SignupRequest;
import com.tourfolio.app.dto.SocialAuthResponse;
import com.tourfolio.app.service.KakaoAuthService;
import com.tourfolio.app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "인증 (Auth)", description = "회원가입, 로그인, 소셜 로그인 API")
public class AuthController {

    private final UserService userService;
    private final KakaoAuthService kakaoAuthService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자 계정을 생성합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이메일 또는 닉네임 중복")
    })
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        log.info("회원가입 요청: email={}", request.getEmail());
        AuthResponse response = userService.signup(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "로그인 정보 불일치")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("로그인 요청: email={}", request.getEmail());
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/kakao")
    @Operation(summary = "카카오 소셜 로그인", description = "네이티브 카카오 SDK로 발급받은 액세스 토큰을 사용하여 소셜 로그인을 수행합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "소셜 로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 액세스 토큰"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "카카오 API 호출 실패")
    })
    public ResponseEntity<SocialAuthResponse> kakaoLogin(@Valid @RequestBody KakaoAuthRequest request) {
        log.info("카카오 소셜 로그인 요청 (네이티브 액세스 토큰)");
        SocialAuthResponse response = kakaoAuthService.kakaoLoginWithAccessToken(request.getAccessToken());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/kakao/callback")
    @Operation(summary = "카카오 소셜 로그인 콜백", description = "카카오 인가 서버에서 리다이렉트된 인가 코드를 받아 소셜 로그인을 처리합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "소셜 로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 인가 코드"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "카카오 API 호출 실패")
    })
    public ResponseEntity<SocialAuthResponse> kakaoCallback(
            @Parameter(description = "카카오 인가 코드", example = "authorization_code_from_kakao", required = true)
            @RequestParam("code") String code) {
        log.info("카카오 소셜 로그인 콜백 요청: code={}", code);
        SocialAuthResponse response = kakaoAuthService.kakaoLogin(code);
        return ResponseEntity.ok(response);
    }
}