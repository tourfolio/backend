package com.tourfolio.app.service;

import com.tourfolio.app.entity.PointHistory;
import com.tourfolio.app.entity.User;
import com.tourfolio.app.dto.AuthResponse;
import com.tourfolio.app.dto.LoginRequest;
import com.tourfolio.app.dto.SignupRequest;
import com.tourfolio.app.exception.CustomException;
import com.tourfolio.app.exception.DuplicateEmailException;
import com.tourfolio.app.exception.DuplicateNicknameException;
import com.tourfolio.app.exception.InvalidCredentialsException;
import com.tourfolio.app.repository.PointHistoryRepository;
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
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PointHistoryRepository pointHistoryRepository;
    private final NotificationService notificationService;

    // 1. 회원가입: 축하 포인트 30,000 지급
    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("이미 가입된 이메일입니다.");
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new DuplicateNicknameException("이미 사용 중인 닉네임입니다.");
        }

        // 회원 생성 (축하 포인트 30,000)
        BigDecimal signupBonus = new BigDecimal("30000");

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .active(true)
                .balance(signupBonus)
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

        log.info("회원가입 성공: userId={}, 지급 잔액={}", savedUser.getId(), savedUser.getBalance());

        return createAuthResponse(savedUser);
    }

    // 2. 로그인
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("로그인 정보가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("로그인 정보가 올바르지 않습니다.");
        }
        if (!user.getActive()) {
            throw new InvalidCredentialsException("비활성화된 계정입니다.");
        }

        return createAuthResponse(user);
    }

    private AuthResponse createAuthResponse(User user) {
        String token = "TOKEN_" + UUID.randomUUID().toString().replace("-", "") + "_" + user.getId();
        return AuthResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .token(token)
                .createdAt(user.getCreatedAt())
                .build();
    }

    // 3. 자산 충전
    @Transactional
    public User chargeBalance(Long memberId, BigDecimal amount) {
        User user = userRepository.findById(memberId)
                .orElseThrow(() -> new CustomException("MEMBER_NOT_FOUND", "회원을 찾을 수 없습니다."));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("INVALID_AMOUNT", "충전 금액은 0보다 커야 합니다.");
        }

        user.setBalance(user.getBalance().add(amount));
        User savedUser = userRepository.save(user);
        log.info("자산 충전 완료: memberId={}, 충전금액={}, 현재잔액={}", memberId, amount, savedUser.getBalance());
        return savedUser;
    }

    // 4. 프로필 수정 (닉네임)
    @Transactional
    public User updateProfile(Long memberId, String newNickname) {
        User user = userRepository.findById(memberId)
                .orElseThrow(() -> new CustomException("MEMBER_NOT_FOUND", "회원을 찾을 수 없습니다."));

        if (!user.getNickname().equals(newNickname) && userRepository.existsByNickname(newNickname)) {
            throw new DuplicateNicknameException("이미 사용 중인 닉네임입니다.");
        }

        user.setNickname(newNickname);
        User savedUser = userRepository.save(user);
        log.info("프로필 수정 완료: memberId={}, 새 닉네임={}", memberId, newNickname);
        return savedUser;
    }
}