// src/main/java/com/tourfolio/app/service/PriceCalculationService.java
package com.tourfolio.app.service;

import com.tourfolio.app.entity.PriceHistory;
import com.tourfolio.app.entity.Spot;
import com.tourfolio.app.entity.Transaction;
import com.tourfolio.app.repository.PriceHistoryRepository;
import com.tourfolio.app.repository.TransactionRepository;
import com.tourfolio.app.util.NormalizationConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceCalculationService {

    private final PriceHistoryRepository priceHistoryRepository;
    private final TransactionRepository transactionRepository;

    public record YesterdayContext(Double yesterdayTS, BigDecimal yesterdayPrice, Double yesterdayP, Double yesterdayD,
                                   Double yesterdayR) {
            public YesterdayContext(Double yesterdayTS, BigDecimal yesterdayPrice) {
                this(yesterdayTS, yesterdayPrice, null, null, null);
            }

    }

    public YesterdayContext getYesterdayContext(Spot spot) {
        try {
            PriceHistory latest = priceHistoryRepository.findFirstBySpotIdOrderByTradeDateDesc(spot.getId());
            if (latest != null) {
                return new YesterdayContext(
                        toDouble(latest.getTsScore()),
                        latest.getPrice(),
                        toDouble(latest.getPScore()),
                        toDouble(latest.getDScore()),
                        toDouble(latest.getRScore())
                );
            }
        } catch (Exception e) {
            log.warn("어제 컨텍스트 조회 실패: spotId={}, error={}", spot.getId(), e.getMessage());
        }
        return null;
    }

    private static Double toDouble(BigDecimal value) {
        return value != null ? value.doubleValue() : null;
    }

    public BigDecimal calculateTodayPrice(Spot spot, YesterdayContext ctx, Double p, Double d, Double r, Double s) {
        if (ctx == null || ctx.yesterdayTS() == null || ctx.yesterdayPrice() == null) {
            return spot.getCurrentPrice();
        }

        try {
            double todayTS = (p * 0.60) + (d * 0.25) + (r * 0.15);
            double yesterdayTS = ctx.yesterdayTS();
            double tsChange = (yesterdayTS != 0) ? (todayTS - yesterdayTS) / yesterdayTS : 0.0;
            double us = calculateUserSentiment(spot);

            double raw = ((tsChange * 0.8) + (us * 0.2)) * s;
            double finalChange = NormalizationConstants.clampFinalChange(raw);

            BigDecimal newPrice = ctx.yesterdayPrice().multiply(BigDecimal.valueOf(1.0 + finalChange));
            // 10원 단위 반올림 처리
            return newPrice.divide(BigDecimal.valueOf(10), 0, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(10));
        } catch (Exception e) {
            log.error("가격 계산 오류: spotId={}, error={}", spot.getId(), e.getMessage());
            return spot.getCurrentPrice();
        }
    }

    private double calculateUserSentiment(Spot spot) {
        try {
            LocalDateTime yesterdayStart = LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime yesterdayEnd = LocalDateTime.now().minusDays(1).withHour(23).withMinute(59).withSecond(59);

            // [수정 완료] 레포지토리와 메서드명 일치
            List<Transaction> transactions = transactionRepository.findBySpotIdAndCreatedAtBetweenOrderByCreatedAtAsc(
                    spot.getId(), yesterdayStart, yesterdayEnd
            );

            if (transactions.isEmpty()) return 0.0;

            BigDecimal buyVol = BigDecimal.ZERO;
            BigDecimal sellVol = BigDecimal.ZERO;

            for (Transaction tx : transactions) {
                if ("BUY".equals(tx.getType())) buyVol = buyVol.add(tx.getQuantity());
                else if ("SELL".equals(tx.getType())) sellVol = sellVol.add(tx.getQuantity());
            }

            BigDecimal totalVol = buyVol.add(sellVol);
            if (totalVol.compareTo(BigDecimal.ZERO) == 0) return 0.0;

            double us = buyVol.subtract(sellVol).divide(totalVol, 4, RoundingMode.HALF_UP).doubleValue();
            return Math.max(-1.0, Math.min(1.0, us));
        } catch (Exception e) {
            log.warn("US 계산 실패: spotId={}, error={}", spot.getId(), e.getMessage());
            return 0.0;
        }
    }
}