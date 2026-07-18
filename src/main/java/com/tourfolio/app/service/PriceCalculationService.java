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

/**
 * 가격 계산 서비스
 * 기획서 기반 주가 변동 알고리즘 구현
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PriceCalculationService {

    private final PriceHistoryRepository priceHistoryRepository;
    private final TransactionRepository transactionRepository;

    /**
     * 어제 컨텍스트 (어제 TS와 가격 정보)
     */
    public static class YesterdayContext {
        private final Double yesterdayTS;
        private final BigDecimal yesterdayPrice;

        public YesterdayContext(Double yesterdayTS, BigDecimal yesterdayPrice) {
            this.yesterdayTS = yesterdayTS;
            this.yesterdayPrice = yesterdayPrice;
        }

        public Double getYesterdayTS() {
            return yesterdayTS;
        }

        public BigDecimal getYesterdayPrice() {
            return yesterdayPrice;
        }
    }

    /**
     * 어제 컨텍스트 조회
     * @param spot 관광지 엔티티
     * @return 어제 컨텍스트 (데이터 없으면 null)
     */
    public YesterdayContext getYesterdayContext(Spot spot) {
        try {
            // price_history에서 spot_id 기준 최신 1건 조회
            PriceHistory latestHistory = priceHistoryRepository.findFirstBySpotIdOrderByTradeDateDesc(spot.getId());
            
            if (latestHistory != null) {
                return new YesterdayContext(
                        latestHistory.getTsScore() != null ? latestHistory.getTsScore().doubleValue() : null,
                        latestHistory.getPrice()
                );
            }
        } catch (Exception e) {
            log.warn("어제 컨텍스트 조회 실패: spotId={}, error={}", spot.getId(), e.getMessage());
        }
        return null;
    }

    /**
     * 오늘 가격 계산
     * @param spot 관광지 엔티티
     * @param ctx 어제 컨텍스트
     * @param p 정규화된 P 값 (0~1)
     * @param d 정규화된 D 값 (0~1)
     * @param r 정규화된 R 값 (0~1)
     * @param s S 계수 (0.9, 1.0, 1.1, 1.2)
     * @return 계산된 오늘 가격
     */
    public BigDecimal calculateTodayPrice(Spot spot, YesterdayContext ctx, Double p, Double d, Double r, Double s) {
        if (ctx == null || ctx.getYesterdayTS() == null || ctx.getYesterdayPrice() == null) {
            log.warn("어제 데이터 없음으로 가격 계산 불가: spotId={}", spot.getId());
            return spot.getCurrentPrice(); // 기존 가격 유지
        }

        try {
            // 1. TS = P×0.60 + D×0.25 + R×0.15
            double todayTS = (p * 0.60) + (d * 0.25) + (r * 0.15);
            log.debug("TS 계산: spot={}, P={}, D={}, R={}, TS={}", spot.getName(), p, d, r, todayTS);

            // 2. TS_change = (TS_오늘 − TS_어제) / TS_어제
            double yesterdayTS = ctx.getYesterdayTS();
            double tsChange;
            if (yesterdayTS != 0) {
                tsChange = (todayTS - yesterdayTS) / yesterdayTS;
            } else {
                tsChange = 0.0;
            }
            log.debug("TS_change 계산: spot={}, TS_오늘={}, TS_어제={}, TS_change={}", 
                    spot.getName(), todayTS, yesterdayTS, tsChange);

            // 3. US = (매수량 − 매도량) / 전체거래량
            double us = calculateUserSentiment(spot);
            log.debug("US 계산: spot={}, US={}", spot.getName(), us);

            // 4. raw = (TS_change × 0.8 + US × 0.2) × S
            double raw = ((tsChange * 0.8) + (us * 0.2)) * s;
            log.debug("raw 계산: spot={}, TS_change={}, US={}, S={}, raw={}", 
                    spot.getName(), tsChange, us, s, raw);

            // 5. FinalChange = clamp(raw, −0.10, +0.10)
            double finalChange = NormalizationConstants.clampFinalChange(raw);
            log.debug("FinalChange 계산: spot={}, raw={}, FinalChange={}", 
                    spot.getName(), raw, finalChange);

            // 6. Price_오늘 = Price_어제 × (1 + FinalChange)
            BigDecimal yesterdayPrice = ctx.getYesterdayPrice();
            BigDecimal todayPrice = yesterdayPrice.multiply(
                    BigDecimal.valueOf(1.0 + finalChange)
            ).setScale(0, RoundingMode.HALF_UP);

            log.info("가격 계산 완료: spot={}, 어제가격={}, 오늘가격={}, 변동률={}%", 
                    spot.getName(), yesterdayPrice, todayPrice, finalChange * 100);

            return todayPrice;

        } catch (Exception e) {
            log.error("가격 계산 중 오류: spotId={}, error={}", spot.getId(), e.getMessage());
            return spot.getCurrentPrice(); // 기존 가격 유지
        }
    }

    /**
     * 사용자 감정 (US) 계산
     * US = (매수량 − 매도량) / 전체거래량
     * 거래 없으면 0, 범위 −1~+1
     * @param spot 관광지 엔티티
     * @return US 값 (-1~+1)
     */
    private double calculateUserSentiment(Spot spot) {
        try {
            // 전날 하루치 거래 조회
            LocalDateTime yesterdayStart = LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime yesterdayEnd = LocalDateTime.now().minusDays(1).withHour(23).withMinute(59).withSecond(59);

            List<Transaction> yesterdayTransactions = transactionRepository.findBySpotIdAndCreatedAtBetween(
                    spot.getId(), 
                    yesterdayStart, 
                    yesterdayEnd
            );

            if (yesterdayTransactions.isEmpty()) {
                return 0.0;
            }

            BigDecimal buyVolume = BigDecimal.ZERO;
            BigDecimal sellVolume = BigDecimal.ZERO;

            for (Transaction tx : yesterdayTransactions) {
                if ("BUY".equals(tx.getType())) {
                    buyVolume = buyVolume.add(tx.getQuantity());
                } else if ("SELL".equals(tx.getType())) {
                    sellVolume = sellVolume.add(tx.getQuantity());
                }
            }

            BigDecimal totalVolume = buyVolume.add(sellVolume);
            if (totalVolume.compareTo(BigDecimal.ZERO) == 0) {
                return 0.0;
            }

            double us = buyVolume.subtract(sellVolume)
                    .divide(totalVolume, 4, RoundingMode.HALF_UP)
                    .doubleValue();

            // 범위 제한 (-1~+1)
            return Math.max(-1.0, Math.min(1.0, us));

        } catch (Exception e) {
            log.warn("US 계산 실패: spotId={}, error={}", spot.getId(), e.getMessage());
            return 0.0;
        }
    }
}
