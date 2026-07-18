package com.tourfolio.app.service;

import com.tourfolio.app.entity.Spot;
import com.tourfolio.app.repository.PriceHistoryRepository;
import com.tourfolio.app.repository.TransactionRepository;
import com.tourfolio.app.util.NormalizationConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * PriceCalculationService 단위 테스트
 * 신버전(min-max 정규화) 기준 TS 계산 검증
 */
@ExtendWith(MockitoExtension.class)
class PriceCalculationServiceTest {

    @Mock
    private PriceHistoryRepository priceHistoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private PriceCalculationService priceCalculationService;

    private Spot testSpot;
    private PriceCalculationService.YesterdayContext yesterdayContext;

    @BeforeEach
    void setUp() {
        // 테스트용 관광지 생성
        testSpot = Spot.builder()
                .id(1L)
                .name("테스트 관광지")
                .currentPrice(new BigDecimal("10000"))
                .build();

        // 어제 컨텍스트 생성 (어제 TS=0.50, 어제 가격=10000)
        yesterdayContext = new PriceCalculationService.YesterdayContext(0.50, new BigDecimal("10000"));
    }

    @Test
    void testCalculateTodayPrice_WithRealNormalizedValues() {
        // Given: 실제 정규화 공식 기준값
        // P=68 → 정규화: (68-8.31)/91.69 = 0.654
        // D=92 → 체류 정규화: (92-60.46)/64.74 = 0.487, 소비 정규화: (92-54.09)/92.93 = 0.408, 평균: 0.448
        // R=110 → 서비스 정규화: (110-63.55)/88.94 = 0.522, 문화 정규화: (110-64.24)/71.97 = 0.639, 평균: 0.581
        // S=1.0 (기본값)
        
        double p = NormalizationConstants.normalizeP(68.0);
        double d = (NormalizationConstants.normalizeDStay(92.0) + NormalizationConstants.normalizeDSpend(92.0)) / 2.0;
        double r = (NormalizationConstants.normalizeRService(110.0) + NormalizationConstants.normalizeRCulture(110.0)) / 2.0;
        double s = 1.0;

        // When: 가격 계산
        BigDecimal todayPrice = priceCalculationService.calculateTodayPrice(
                testSpot, yesterdayContext, p, d, r, s
        );

        // Then: TS 계산 검증
        // TS = P×0.60 + D×0.25 + R×0.15
        double expectedTS = (p * 0.60) + (d * 0.25) + (r * 0.15);
        System.out.println("P (정규화): " + p);
        System.out.println("D (정규화): " + d);
        System.out.println("R (정규화): " + r);
        System.out.println("예상 TS: " + expectedTS);

        // TS_change = (TS_오늘 − TS_어제) / TS_어제
        double tsChange = (expectedTS - 0.50) / 0.50;
        System.out.println("TS_change: " + tsChange);

        // US = 0 (거래 없음)
        // raw = (TS_change × 0.8 + US × 0.2) × S
        double raw = (tsChange * 0.8 + 0.0 * 0.2) * s;
        System.out.println("raw: " + raw);

        // FinalChange = clamp(raw, −0.10, +0.10)
        double finalChange = NormalizationConstants.clampFinalChange(raw);
        System.out.println("FinalChange: " + finalChange);

        // Price_오늘 = Price_어제 × (1 + FinalChange)
        BigDecimal expectedPrice = new BigDecimal("10000")
                .multiply(BigDecimal.valueOf(1.0 + finalChange))
                .setScale(0, RoundingMode.HALF_UP);
        System.out.println("예상 가격: " + expectedPrice);
        System.out.println("실제 가격: " + todayPrice);

        // 가격 검증 (약간의 오차 허용)
        assertEquals(expectedPrice, todayPrice);

        // TS가 0.59 근처인지 검증
        assertEquals(0.59, expectedTS, 0.01);
    }

    @Test
    void testCalculateTodayPrice_WithZeroYesterdayTS() {
        // Given: 어제 TS가 0인 경우
        PriceCalculationService.YesterdayContext zeroTSContext = 
                new PriceCalculationService.YesterdayContext(0.0, new BigDecimal("10000"));

        double p = 0.5;
        double d = 0.5;
        double r = 0.5;
        double s = 1.0;

        // When
        BigDecimal todayPrice = priceCalculationService.calculateTodayPrice(
                testSpot, zeroTSContext, p, d, r, s
        );

        // Then: 어제 TS가 0이면 TS_change는 0이 되어야 함
        // 따라서 가격 변동 없음
        assertEquals(new BigDecimal("10000"), todayPrice);
    }

    @Test
    void testCalculateTodayPrice_WithNullYesterdayContext() {
        // Given: 어제 컨텍스트가 null인 경우
        double p = 0.5;
        double d = 0.5;
        double r = 0.5;
        double s = 1.0;

        // When
        BigDecimal todayPrice = priceCalculationService.calculateTodayPrice(
                testSpot, null, p, d, r, s
        );

        // Then: 기존 가격 유지
        assertEquals(new BigDecimal("10000"), todayPrice);
    }

    @Test
    void testNormalizationConstants_ClampNormalized() {
        // Given
        double value = 1.5;

        // When
        double clamped = NormalizationConstants.clampNormalized(value);

        // Then: 1.5는 1로 clamped 되어야 함
        assertEquals(1.0, clamped);
    }

    @Test
    void testNormalizationConstants_ClampFinalChange() {
        // Given
        double value = 0.15;

        // When
        double clamped = NormalizationConstants.clampFinalChange(value);

        // Then: 0.15는 0.10으로 clamped 되어야 함
        assertEquals(0.10, clamped);
    }
}
