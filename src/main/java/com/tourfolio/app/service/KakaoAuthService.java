package com.tourfolio.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourfolio.app.dto.KakaoTokenResponse;
import com.tourfolio.app.dto.KakaoUserInfoResponse;
import com.tourfolio.app.dto.SocialAuthResponse;
import com.tourfolio.app.entity.Member;
import com.tourfolio.app.repository.MemberRepository;
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
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    /**
     * 카카오 소셜 로그인 처리
     * @param code 카카오 인가 코드
     * @return 소셜 로그인 응답
     */
    @Transactional
    public SocialAuthResponse kakaoLogin(String code) {
        log.info("카카오 소셜 로그인 시작: code={}", code);

        try {
            // 1. 액세스 토큰 요청
            KakaoTokenResponse tokenResponse = getKakaoAccessToken(code);
            log.info("카카오 액세스 토큰 발급 성공");

            // 2. 사용자 정보 조회
            KakaoUserInfoResponse userInfo = getKakaoUserInfo(tokenResponse.getAccessToken());
            log.info("카카오 사용자 정보 조회 성공: id={}, email={}", userInfo.getId(), userInfo.getKakaoAccount().getEmail());

            // 3. 기존 회원 확인
            String providerId = String.valueOf(userInfo.getId());
            Member member = memberRepository.findByProviderAndProviderId("KAKAO", providerId)
                    .orElse(null);

            boolean isNewMember = false;

            // 4. 신규 회원인 경우 자동 가입
            if (member == null) {
                member = createKakaoMember(userInfo);
                isNewMember = true;
                log.info("카카오 신규 회원 가입 완료: memberId={}, email={}", member.getId(), member.getEmail());
            } else {
                log.info("카카오 기존 회원 로그인: memberId={}, email={}", member.getId(), member.getEmail());
            }

            // 5. 응답 생성
            return SocialAuthResponse.builder()
                    .id(member.getId())
                    .email(member.getEmail())
                    .nickname(member.getNickname())
                    .token(generateToken(member))
                    .isNewMember(isNewMember)
                    .createdAt(member.getCreatedAt())
                    .build();

        } catch (Exception e) {
            log.error("카카오 소셜 로그인 실패", e);
            throw new RuntimeException("카카오 소셜 로그인 실패: " + e.getMessage());
        }
    }

    /**
     * 카카오 액세스 토큰 발급
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
     * 카카오 사용자 정보 조회
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
     * 카카오 회원 생성
     */
    private Member createKakaoMember(KakaoUserInfoResponse userInfo) {
        String email = userInfo.getKakaoAccount().getEmail();
        String nickname = userInfo.getKakaoAccount().getProfile().getNickname();
        String providerId = String.valueOf(userInfo.getId());

        // 닉네임 중복 확인
        if (memberRepository.existsByNickname(nickname)) {
            nickname = nickname + "_" + UUID.randomUUID().toString().substring(0, 8);
        }

        Member member = Member.builder()
                .email(email)
                .password("") // 소셜 로그인은 비밀번호 불필요
                .nickname(nickname)
                .active(true)
                .balance(new BigDecimal("50000")) // 회원가입 축하 포인트
                .provider("KAKAO")
                .providerId(providerId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return memberRepository.save(member);
    }

    /**
     * 토큰 생성
     */
    private String generateToken(Member member) {
        return "TOKEN_" + UUID.randomUUID().toString().replace("-", "") + "_" + member.getId();
    }
}
