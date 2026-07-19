package com.tourfolio.app.service;

import com.tourfolio.app.entity.Attendance;
import com.tourfolio.app.entity.Member;
import com.tourfolio.app.dto.AuthResponse;
import com.tourfolio.app.dto.LoginRequest;
import com.tourfolio.app.dto.SignupRequest;
import com.tourfolio.app.exception.*;
import com.tourfolio.app.repository.AttendanceRepository;
import com.tourfolio.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true) // 읽기 전용으로 기본 설정
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AttendanceRepository attendanceRepository;

    // 1. 회원가입: 축하 포인트 지급 포함
    @Transactional
    public AuthResponse signup(SignupRequest request) {
        log.info("회원가입 처리 시작: email={}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("이미 가입된 이메일입니다.");
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new DuplicateNicknameException("이미 사용 중인 닉네임입니다.");
        }

        // 회원 생성 (축하 포인트 50,000 고정)
        Member member = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .active(true)
                .balance(new BigDecimal("50000"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Member savedMember = userRepository.save(member);
        log.info("회원가입 성공: userId={}", savedMember.getId());

        return createAuthResponse(savedMember);
    }

    // 2. 로그인: 출석체크 로직 연동
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Member member = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("로그인 정보가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new InvalidCredentialsException("로그인 정보가 올바르지 않습니다.");
        }
        if (!member.getActive()) {
            throw new InvalidCredentialsException("비활성화된 계정입니다.");
        }

        // 출석체크 수행
        checkAndAwardAttendance(member);

        return createAuthResponse(member);
    }

    private void checkAndAwardAttendance(Member member) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.getHour() < 10
                ? now.toLocalDate().minusDays(1).atTime(10, 0)
                : now.toLocalDate().atTime(10, 0);

        boolean alreadyAttended = attendanceRepository.existsByMemberIdAndAttendanceDateAfter(member.getId(), threshold);

        if (!alreadyAttended) {
            member.setBalance(member.getBalance().add(new BigDecimal("1000")));
            userRepository.save(member);

            attendanceRepository.save(Attendance.builder()
                    .memberId(member.getId())
                    .attendanceDate(now)
                    .pointsAwarded(1000)
                    .build());
            log.info("출석 포인트 지급 완료: memberId={}, 지급액=1000", member.getId());
        }
    }

    private AuthResponse createAuthResponse(Member member) {
        String token = "TOKEN_" + UUID.randomUUID().toString().replace("-", "") + "_" + member.getId();
        return AuthResponse.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .token(token)
                .createdAt(member.getCreatedAt())
                .build();
    }
}