package com.tourfolio.app.service;

import com.tourfolio.app.entity.Attendance;
import com.tourfolio.app.entity.Member;
import com.tourfolio.app.dto.AuthResponse;
import com.tourfolio.app.dto.LoginRequest;
import com.tourfolio.app.dto.SignupRequest;
import com.tourfolio.app.exception.DuplicateEmailException;
import com.tourfolio.app.exception.DuplicateNicknameException;
import com.tourfolio.app.exception.InvalidCredentialsException;
import com.tourfolio.app.exception.UserNotFoundException;
import com.tourfolio.app.repository.AttendanceRepository;
import com.tourfolio.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AttendanceRepository attendanceRepository;

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

        // 사용자 생성 (회원가입 축하 포인트 50,000 포인트 지급)
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

        // 일일 출석체크 로직
        checkAndAwardAttendance(member);

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

    @Transactional
    private void checkAndAwardAttendance(Member member) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime today10AM = now.toLocalDate().atTime(10, 0);
        LocalDateTime yesterday10AM = today10AM.minusDays(1);

        LocalDateTime checkStartTime;

        if (now.isBefore(today10AM)) {
            // 오전 10시 이전: 어제 오전 10시 이후 출석 기록 확인
            checkStartTime = yesterday10AM;
            log.debug("오전 10시 이전: 어제 10시 이후 출석 기록 확인 (기준 시간: {})", checkStartTime);
        } else {
            // 오전 10시 이후: 오늘 오전 10시 이후 출석 기록 확인
            checkStartTime = today10AM;
            log.debug("오전 10시 이후: 오늘 10시 이후 출석 기록 확인 (기준 시간: {})", checkStartTime);
        }

        // 출석 기록 확인
        boolean alreadyAttended = attendanceRepository.existsByMemberIdAndAttendanceDateAfter(
                member.getId(), checkStartTime);

        if (!alreadyAttended) {
            // 출석체크 가능: 1,000 포인트 지급
            member.setBalance(member.getBalance().add(new BigDecimal("1000")));
            member.setUpdatedAt(LocalDateTime.now());
            userRepository.save(member);

            // 출석 기록 생성
            Attendance attendance = Attendance.builder()
                    .memberId(member.getId())
                    .attendanceDate(now)
                    .pointsAwarded(1000)
                    .build();
            attendanceRepository.save(attendance);

            log.info("출석체크 완료: memberId={}, 지급 포인트=1,000, 현재 잔액={}", member.getId(), member.getBalance());
        } else {
            log.debug("이미 출석 완료: memberId={}", member.getId());
        }
    }
}
