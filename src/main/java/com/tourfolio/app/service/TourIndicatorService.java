package com.tourfolio.app.service;

import com.tourfolio.app.api.client.AreaTarDemDsClient;
import com.tourfolio.app.api.client.AreaTarResDemClient;
import com.tourfolio.app.api.client.DataLabClient;
import com.tourfolio.app.api.client.TatsCnctrRateClient;
import com.tourfolio.app.api.dto.AreaTarDemDsDto;
import com.tourfolio.app.api.dto.AreaTarResDemDto;
import com.tourfolio.app.api.dto.DataLabDto;
import com.tourfolio.app.api.dto.TatsCnctrRateDto;
import com.tourfolio.app.entity.Spot;
import com.tourfolio.app.util.NormalizationConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 관광 지표 수집 서비스
 *
 * P(60%) 관광지 집중률   - 일 단위, 향후 7일 예측 평균
 * D(25%) 관광 수요 강도   - 월 단위(체류/소비 평균)
 * R(15%) 관광 자원 수요   - 월 단위(서비스/문화 평균)
 * S      시즌 계수        - 전국 단일값, 전달 대비 전전달 증감률
 *
 * 시군구/전국 단위로 캐싱하여 배치 1회당 API 중복 호출을 막는다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TourIndicatorService {

    private static final DateTimeFormatter YM = DateTimeFormatter.ofPattern("yyyyMM");

    /** P: 향후 30일 예측 중 사용할 일수 (멀수록 노이즈가 커져 앞 7일만 사용) */
    private static final int P_FORECAST_DAYS = 7;

    /** DataLab 방문자수는 약 30일 지연 발행되므로 그만큼 뒤로 물러나서 조회한다 */
    private static final int DATALAB_LAG_DAYS = 30;

    /** D/R 월간 지표는 매월 16일 이후에 전달 데이터가 공개된다 */
    private static final int MONTHLY_PUBLISH_DAY = 16;

    private final TatsCnctrRateClient tatsCnctrRateClient;
    private final AreaTarDemDsClient areaTarDemDsClient;
    private final AreaTarResDemClient areaTarResDemClient;
    private final DataLabClient dataLabClient;

    // 시군구 단위 D/R 캐시 (같은 구의 종목끼리 공유)
    private final Map<String, Double> dCache = new ConcurrentHashMap<>();
    private final Map<String, Double> rCache = new ConcurrentHashMap<>();

    // S는 전국 단일 계수라 스칼라 하나만 들고 있으면 된다
    private volatile Double cachedS = null;

    /**
     * D/R 조회 기준연월.
     * 매월 16일 이후면 전달, 그 전이면 전전달 데이터가 최신이다.
     */
    String resolveMonthlyBaseYm(LocalDate today) {
        YearMonth base = YearMonth.from(today).minusMonths(1);
        if (today.getDayOfMonth() < MONTHLY_PUBLISH_DAY) {
            base = base.minusMonths(1);
        }
        return base.format(YM);
    }

    /**
     * P (관광지 집중률 예측) 수집.
     * 향후 30일 예측 중 오늘부터 7일치만 평균 내어 사용한다.
     *
     * @return 정규화된 P 값 (0~1), 실패 시 previousP
     */
    public Double collectP(Spot spot, Double previousP) {
        try {
            List<TatsCnctrRateDto> predictions = tatsCnctrRateClient.fetchPredictions(
                    spot.getAreaCode(), spot.getSignguCd(), spot.getName());

            if (predictions != null && !predictions.isEmpty()) {
                LocalDate today = LocalDate.now();

                List<Double> window = predictions.stream()
                        .filter(dto -> dto.getBaseDate() != null && !dto.getBaseDate().isBefore(today))
                        .sorted(Comparator.comparing(TatsCnctrRateDto::getBaseDate))
                        .limit(P_FORECAST_DAYS)
                        .map(TatsCnctrRateDto::getPredictedValue)
                        .filter(Objects::nonNull)
                        .toList();

                if (!window.isEmpty()) {
                    double avgRate = window.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                    double normalizedP = NormalizationConstants.normalizeP(avgRate);
                    log.debug("P 수집 성공: spot={}, {}일 평균={}, 정규화={}",
                            spot.getName(), window.size(), avgRate, normalizedP);
                    return normalizedP;
                }
            }
        } catch (Exception e) {
            log.warn("P 수집 실패: spot={}, error={}", spot.getName(), e.getMessage());
        }

        log.debug("P 폴백: spot={}, 이전값={}", spot.getName(), previousP);
        return previousP;
    }

    /**
     * D (지역별 관광 수요 강도) 수집 = 체류 강도, 소비 강도 정규화 평균.
     * 시군구 단위 캐싱.
     */
    public Double collectD(String areaCd, String signguCd, Double previousD) {
        String cacheKey = areaCd + ":" + signguCd;
        Double cached = dCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        try {
            String baseYm = resolveMonthlyBaseYm(LocalDate.now());

            Double stayValue = firstValue(
                    areaTarDemDsClient.fetchStayIntensity(areaCd, signguCd, baseYm),
                    AreaTarDemDsDto::getStayValue);
            Double spendValue = firstValue(
                    areaTarDemDsClient.fetchSpendIntensity(areaCd, signguCd, baseYm),
                    AreaTarDemDsDto::getSpendValue);

            if (stayValue != null && spendValue != null) {
                double d = (NormalizationConstants.normalizeDStay(stayValue)
                        + NormalizationConstants.normalizeDSpend(spendValue)) / 2.0;
                dCache.put(cacheKey, d);
                log.debug("D 수집 성공: {}/{} {} 체류={} 소비={} -> {}",
                        areaCd, signguCd, baseYm, stayValue, spendValue, d);
                return d;
            }
        } catch (Exception e) {
            log.warn("D 수집 실패: {}/{}, error={}", areaCd, signguCd, e.getMessage());
        }

        log.debug("D 폴백: {}/{}, 이전값={}", areaCd, signguCd, previousD);
        return previousD;
    }

    /**
     * R (지역별 관광 자원 수요) 수집 = 서비스 수요, 문화 자원 수요 정규화 평균.
     * 시군구 단위 캐싱.
     */
    public Double collectR(String areaCd, String signguCd, Double previousR) {
        String cacheKey = areaCd + ":" + signguCd;
        Double cached = rCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        try {
            String baseYm = resolveMonthlyBaseYm(LocalDate.now());

            Double serviceValue = firstValue(
                    areaTarResDemClient.fetchServiceDemand(areaCd, signguCd, baseYm),
                    AreaTarResDemDto::getServiceValue);
            Double cultureValue = firstValue(
                    areaTarResDemClient.fetchCultureDemand(areaCd, signguCd, baseYm),
                    AreaTarResDemDto::getCultureValue);

            if (serviceValue != null && cultureValue != null) {
                double r = (NormalizationConstants.normalizeRService(serviceValue)
                        + NormalizationConstants.normalizeRCulture(cultureValue)) / 2.0;
                rCache.put(cacheKey, r);
                log.debug("R 수집 성공: {}/{} {} 서비스={} 문화={} -> {}",
                        areaCd, signguCd, baseYm, serviceValue, cultureValue, r);
                return r;
            }
        } catch (Exception e) {
            log.warn("R 수집 실패: {}/{}, error={}", areaCd, signguCd, e.getMessage());
        }

        log.debug("R 폴백: {}/{}, 이전값={}", areaCd, signguCd, previousR);
        return previousR;
    }

    /**
     * S (시즌 계수) 수집.
     *
     * 전국 방문자수는 각 지역 방문자의 합산이라 공식에 직접 넣으면 D와 신호가 중복된다.
     * 따라서 모든 종목에 동일하게 곱해지는 "시장 온도계"로만 쓴다.
     * 기준은 전달 대비 전전달 증감률(외지인 기준, 전국 합산).
     *
     * @return 0.9 / 1.0 / 1.1 / 1.2 중 하나, 실패 시 1.0
     */
    public Double collectS() {
        Double cached = cachedS;
        if (cached != null) {
            return cached;
        }

        try {
            // 발행 지연을 감안해 30일 물러난 시점을 기준으로 달력 월을 잡는다
            YearMonth anchor = YearMonth.from(LocalDate.now().minusDays(DATALAB_LAG_DAYS));
            YearMonth prevMonth = anchor;
            YearMonth prevPrevMonth = anchor.minusMonths(1);

            double prevSum = sumNationwideOutsiders(prevMonth);
            double prevPrevSum = sumNationwideOutsiders(prevPrevMonth);

            if (prevPrevSum > 0 && prevSum > 0) {
                double growthRate = (prevSum - prevPrevSum) / prevPrevSum * 100.0;
                double s = resolveSeasonCoefficient(growthRate);
                cachedS = s;
                log.info("S 계수 산출: {}({}) 대비 {}({}) 증감률={}% -> S={}",
                        prevPrevMonth, String.format("%.0f", prevPrevSum),
                        prevMonth, String.format("%.0f", prevSum),
                        String.format("%.2f", growthRate), s);
                return s;
            }
            log.warn("S 계수 산출 불가: 전달합={} 전전달합={}", prevSum, prevPrevSum);
        } catch (Exception e) {
            log.warn("S 계수 수집 실패: error={}", e.getMessage());
        }

        cachedS = 1.0;
        log.debug("S 폴백: 기본값 1.0");
        return 1.0;
    }

    /** 해당 월의 전국 외지인 방문자수 합계 */
    private double sumNationwideOutsiders(YearMonth month) {
        List<DataLabDto> rows = dataLabClient.fetchVisitorCounts(
                month.atDay(1), month.atEndOfMonth());

        if (rows == null || rows.isEmpty()) {
            return 0.0;
        }
        return rows.stream()
                .filter(DataLabDto::isOutsider)
                .map(DataLabDto::getVisitorCount)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    /** 시즌 계수 적용표 */
    double resolveSeasonCoefficient(double growthRatePercent) {
        if (growthRatePercent <= -5.0) return 0.9;    // 비수기
        if (growthRatePercent <= 5.0) return 1.0;     // 평상시
        if (growthRatePercent <= 15.0) return 1.1;    // 연휴·성수기
        return 1.2;                                   // 황금연휴·피크
    }

    private <T> Double firstValue(List<T> rows, java.util.function.Function<T, Double> extractor) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        return extractor.apply(rows.get(0));
    }

    /** 매일 배치 시작 전 캐시 초기화 */
    public void clearCache() {
        dCache.clear();
        rCache.clear();
        cachedS = null;
        log.info("지표 캐시 초기화 완료");
    }
}
