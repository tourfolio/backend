package com.tourfolio.app.service;

import com.tourfolio.app.dto.KakaoTokenResponse;
import com.tourfolio.app.dto.KakaoUserInfoResponse;
import com.tourfolio.app.dto.SocialAuthResponse;
import com.tourfolio.app.entity.PointHistory;
import com.tourfolio.app.entity.User;
import com.tourfolio.app.repository.PointHistoryRepository;
import com.tourfolio.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class KakaoAuthService {

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final NotificationService notificationService;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    /**
     * [웹 리다이렉트 방식] 카카오 인가 코드로 로그인 처리 (GET /kakao/callback 전용)
     * 서버가 code→토큰 교환을 직접 수행하므로 redirect_uri가 정확히 일치해야 한다.
     */
    @Transactional
    public SocialAuthResponse kakaoLogin(String code) {
        log.info("카카오 소셜 로그인 시작 (인가 코드 방식): code={}", code);
        try {
            KakaoTokenResponse tokenResponse = getKakaoAccessToken(code);
            log.info("카카오 액세스 토큰 발급 성공");
            return processKakaoUser(tokenResponse.getAccessToken());
        } catch (Exception e) {
            log.error("카카오 소셜 로그인 실패 (인가 코드 방식)", e);
            throw new RuntimeException("카카오 소셜 로그인 실패: " + e.getMessage());
        }
    }

    /**
     * [네이티브 SDK 방식] 프론트가 카카오 SDK로 이미 발급받은 액세스 토큰으로 로그인 처리 (POST /kakao 전용)
     * 코드→토큰 교환 단계가 없으므로 redirect_uri 검증과 무관하다.
     */
    @Transactional
    public SocialAuthResponse kakaoLoginWithAccessToken(String accessToken) {
        log.info("카카오 소셜 로그인 시작 (네이티브 액세스 토큰 방식)");
        try {
            return processKakaoUser(accessToken);
        } catch (Exception e) {
            log.error("카카오 소셜 로그인 실패 (네이티브 액세스 토큰 방식)", e);
            throw new RuntimeException("카카오 소셜 로그인 실패: " + e.getMessage());
        }
    }

    /**
     * 카카오 액세스 토큰을 받은 이후의 공통 처리 (사용자 조회/가입/토큰 발급)
     */
    private SocialAuthResponse processKakaoUser(String kakaoAccessToken) {
        KakaoUserInfoResponse userInfo = getKakaoUserInfo(kakaoAccessToken);
        log.info("카카오 사용자 정보 조회 성공: id={}, email={}", userInfo.getId(), userInfo.getKakaoAccount().getEmail());

        String providerId = String.valueOf(userInfo.getId());
        User user = userRepository.findByProviderAndProviderId("KAKAO", providerId)
                .orElse(null);

        boolean isNewMember = false;

        if (user == null) {
            user = createKakaoMember(userInfo);
            isNewMember = true;
            log.info("카카오 신규 회원 가입 완료: userId={}, email={}", user.getId(), user.getEmail());
        } else {
            log.info("카카오 기존 회원 로그인: userId={}, email={}", user.getId(), user.getEmail());
        }

        return SocialAuthResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .token(generateToken(user))
                .isNewMember(isNewMember)
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * 카카오 액세스 토큰 발급 (웹 리다이렉트 방식 전용)
     */
    private KakaoTokenResponse getKakaoAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<KakaoTokenResponse> response = restTemplate.postForEntity(
                KAKAO_TOKEN_URL,
                request,
                KakaoTokenResponse.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody();
        }

        throw new RuntimeException("카카오 액세스 토큰 발급 실패");
    }

    /**
     * 카카오 사용자 정보 조회 (공통)
     */
    private KakaoUserInfoResponse getKakaoUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<KakaoUserInfoResponse> response = restTemplate.exchange(
                KAKAO_USER_INFO_URL,
                HttpMethod.GET,
                request,
                KakaoUserInfoResponse.class
        );

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody();
        }

        throw new RuntimeException("카카오 사용자 정보 조회 실패");
    }

    /**
     * 카카오 회원 생성 (이메일 가입과 동일한 정책: 30,000P + 포인트내역 + 알림)
     */
    private User createKakaoMember(KakaoUserInfoResponse userInfo) {
        String email = userInfo.getKakaoAccount().getEmail();
        String nickname = userInfo.getKakaoAccount().getProfile().getNickname();
        String providerId = String.valueOf(userInfo.getId());

        if (userRepository.existsByNickname(nickname)) {
            nickname = nickname + "_" + UUID.randomUUID().toString().substring(0, 8);
        }

        BigDecimal signupBonus = new BigDecimal("30000");

        User user = User.builder()
                .email(email)
                .password("")
                .nickname(nickname)
                .active(true)
                .balance(signupBonus)
                .provider("KAKAO")
                .providerId(providerId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        pointHistoryRepository.save(PointHistory.builder()
                .userId(savedUser.getId())
                .type("SIGNUP")
                .title("회원가입 축하 포인트")
                .amount(signupBonus.longValue())
                .createdAt(LocalDateTime.now())
                .build());

        notificationService.notify(savedUser.getId(), "SIGNUP_BONUS",
                "회원가입 축하 포인트로 " + signupBonus.longValue() + "P가 지급되었습니다");

        return savedUser;
    }

    private String generateToken(User user) {
        return "TOKEN_" + UUID.randomUUID().toString().replace("-", "") + "_" + user.getId();
    }
}