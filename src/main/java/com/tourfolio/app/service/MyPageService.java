package com.tourfolio.app.service;

import com.tourfolio.app.dto.MyPageResponse;
import com.tourfolio.app.entity.Portfolio;
import com.tourfolio.app.entity.Spot;
import com.tourfolio.app.entity.User;
import com.tourfolio.app.exception.CustomException;
import com.tourfolio.app.repository.PortfolioRepository;
import com.tourfolio.app.repository.SpotRepository;
import com.tourfolio.app.repository.UserCardRepository;
import com.tourfolio.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyPageService {

    private final UserRepository userRepository;
    private final UserCardRepository userCardRepository;
    private final PortfolioRepository portfolioRepository;
    private final SpotRepository spotRepository;

    public MyPageResponse getMyPage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));

        long cardCount = userCardRepository.countByUserId(userId);

        List<Portfolio> portfolios = portfolioRepository.findByMemberId(userId);
        BigDecimal totalEval = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (Portfolio p : portfolios) {
            Spot spot = spotRepository.findById(p.getSpotId()).orElse(null);
            if (spot == null) continue;
            totalEval = totalEval.add(spot.getCurrentPrice().multiply(p.getQuantity()));
            totalCost = totalCost.add(p.getAveragePurchasePrice().multiply(p.getQuantity()));
        }

        BigDecimal profitRate = BigDecimal.ZERO;
        if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
            profitRate = totalEval.subtract(totalCost)
                    .divide(totalCost, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return MyPageResponse.builder()
                .nickname(user.getNickname())
                .balance(user.getBalance())
                .cardCount(cardCount)
                .totalProfitRate(profitRate)
                .build();
    }

    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));
        user.setActive(false);
        userRepository.save(user);
        log.info("회원 탈퇴 처리 완료: userId={}", userId);
    }
}