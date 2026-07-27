package com.tourfolio.app.service;

import com.tourfolio.app.entity.Attendance;
import com.tourfolio.app.entity.Member;
import com.tourfolio.app.dto.AuthResponse;
import com.tourfolio.app.dto.LoginRequest;
import com.tourfolio.app.dto.SignupRequest;
import com.tourfolio.app.exception.CustomException;
import com.tourfolio.app.exception.DuplicateEmailException;
import com.tourfolio.app.exception.DuplicateNicknameException;
import com.tourfolio.app.exception.InvalidCredentialsException;
import com.tourfolio.app.repository.AttendanceRepository;
import com.tourfolio.app.repository.MemberRepository;
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
@Transactional(readOnly = true)
public class UserService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AttendanceRepository attendanceRepository;

    // 1. 회원가입: 축하 포인트 50,000 지급
    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("이미 가입된 이메일입니다.");
        }
        if (memberRepository.existsByNickname(request.getNickname())) {
            throw new DuplicateNicknameException("이미 사용 중인 닉네임입니다.");
        }

        // 회원 생성 (축하 포인트 50,000)
        BigDecimal signupBonus = new BigDecimal("50000");

        Member member = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .active(true)
                .balance(signupBonus)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Member savedMember = memberRepository.save(member);
        log.info("회원가입 성공: memberId={}, 지급 잔액={}", savedMember.getId(), savedMember.getBalance());

        return createAuthResponse(savedMember);
    }

    // 2. 로그인: 출석체크 로직 연동
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("로그인 정보가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new InvalidCredentialsException("로그인 정보가 올바르지 않습니다.");
        }
        if (!member.getActive()) {
            throw new InvalidCredentialsException("비활성화된 계정입니다.");
        }

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
            BigDecimal attendanceBonus = new BigDecimal("1000");
            member.setBalance(member.getBalance().add(attendanceBonus));
            memberRepository.save(member);

            attendanceRepository.save(Attendance.builder()
                    .memberId(member.getId())
                    .attendanceDate(now)
                    .pointsAwarded(1000)
                    .build());
            log.info("출석 포인트 지급: memberId={}, 현재잔액={}", member.getId(), member.getBalance());
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

    // 3. 자산 충전
    @Transactional
    public Member chargeBalance(Long memberId, BigDecimal amount) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException("MEMBER_NOT_FOUND", "회원을 찾을 수 없습니다."));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("INVALID_AMOUNT", "충전 금액은 0보다 커야 합니다.");
        }

        member.setBalance(member.getBalance().add(amount));
        Member savedMember = memberRepository.save(member);
        log.info("자산 충전 완료: memberId={}, 충전금액={}, 현재잔액={}", memberId, amount, savedMember.getBalance());
        return savedMember;
    }

    // 4. 프로필 수정 (닉네임)
    @Transactional
    public Member updateProfile(Long memberId, String newNickname) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException("MEMBER_NOT_FOUND", "회원을 찾을 수 없습니다."));

        if (!member.getNickname().equals(newNickname) && memberRepository.existsByNickname(newNickname)) {
            throw new DuplicateNicknameException("이미 사용 중인 닉네임입니다.");
        }

        member.setNickname(newNickname);
        Member savedMember = memberRepository.save(member);
        log.info("프로필 수정 완료: memberId={}, 새 닉네임={}", memberId, newNickname);
        return savedMember;
    }
}