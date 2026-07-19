package com.tourfolio.app.service;

import com.tourfolio.app.entity.Member;
import com.tourfolio.app.dto.AuthResponse;
import com.tourfolio.app.dto.LoginRequest;
import com.tourfolio.app.dto.SignupRequest;
import com.tourfolio.app.exception.DuplicateEmailException;
import com.tourfolio.app.exception.DuplicateNicknameException;
import com.tourfolio.app.exception.InvalidCredentialsException;
import com.tourfolio.app.exception.UserNotFoundException;
import com.tourfolio.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        log.info("회원가입 요청: email={}", request.getEmail());

        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("이미 사용 중인 이메일입니다: " + request.getEmail());
        }

        // 닉네임 중복 체크
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new DuplicateNicknameException("이미 사용 중인 닉네임입니다: " + request.getNickname());
        }

        // 사용자 생성
        Member member = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .active(true)
                .balance(java.math.BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Member savedMember = userRepository.save(member);
        log.info("회원가입 성공: userId={}, email={}", savedMember.getId(), savedMember.getEmail());

        // 임시 토큰 생성 (MVP 단계)
        String token = generateTempToken(savedMember);

        return AuthResponse.builder()
                .id(savedMember.getId())
                .email(savedMember.getEmail())
                .nickname(savedMember.getNickname())
                .token(token)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        log.info("로그인 요청: email={}", request.getEmail());

        Member member = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // 활성 상태 체크
        if (!member.getActive()) {
            throw new InvalidCredentialsException("비활성화된 계정입니다.");
        }

        log.info("로그인 성공: userId={}, email={}", member.getId(), member.getEmail());

        // 임시 토큰 생성 (MVP 단계)
        String token = generateTempToken(member);

        return AuthResponse.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .token(token)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public Member getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다: " + id));
    }

    private String generateTempToken(Member member) {
        // MVP 단계에서는 UUID 기반 임시 토큰 사용
        // 실제 프로덕션에서는 JWT 등을 사용해야 함
        return "TOKEN_" + UUID.randomUUID().toString().replace("-", "") + "_" + member.getId();
    }
}
