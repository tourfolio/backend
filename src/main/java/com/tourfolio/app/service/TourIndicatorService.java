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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 관광 지표 수집 서비스
 * P, D, R, S 지표를 수집하고 캐싱하여 API 중복 호출 방지
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TourIndicatorService {

    private final TatsCnctrRateClient tatsCnctrRateClient;
    private final AreaTarDemDsClient areaTarDemDsClient;
    private final AreaTarResDemClient areaTarResDemClient;
    private final DataLabClient dataLabClient;

    // 캐싱: 시군구 단위 D/R 지표 (하루치)
    private final Map<String, Double> dCache = new ConcurrentHashMap<>();
    private final Map<String, Double> rCache = new ConcurrentHashMap<>();
    
    // 캐싱: 광역시도 단위 S 계수 (하루치)
    private final Map<String, Double> sCache = new ConcurrentHashMap<>();
    
    // 캐싱 초기화 플래그
    private volatile boolean sCacheInitialized = false;

    /**
     * P (관광지 집중률 예측) 수집
     * @param spot 관광지 엔티티
     * @param previousP 이전 P 값 (폴백용)
     * @return 정규화된 P 값 (0~1), 실패 시 previousP 반환
     */
    public Double collectP(Spot spot, Double previousP) {
        try {
            // TODO: 향후 30일 예측값 중 7일치만 사용하는 로직 구현 필요
            // 현재는 "오늘자 예측값 1건" 사용
            List<TatsCnctrRateDto> predictions = tatsCnctrRateClient.fetchPredictions(
                    spot.getAreaCode(), 
                    spot.getSignguCd(), 
                    spot.getName()
            );

            if (predictions != null && !predictions.isEmpty()) {
                // 첫 번째 예측값 사용 (오늘자)
                Double predictedValue = predictions.get(0).getPredictedValue();
                if (predictedValue != null) {
                    double normalizedP = NormalizationConstants.normalizeP(predictedValue);
                    log.debug("P 지표 수집 성공: spot={}, 원본값={}, 정규화값={}", 
                            spot.getName(), predictedValue, normalizedP);
                    return normalizedP;
                }
            }
        } catch (Exception e) {
            log.warn("P 지표 수집 실패: spot={}, error={}", spot.getName(), e.getMessage());
        }
        
        // API 실패 시 이전 값 유지 (폴백)
        log.debug("P 지표 폴백: spot={}, 이전값={}", spot.getName(), previousP);
        return previousP;
    }

    /**
     * D (지역별 관광 수요 강도) 수집
     * 시군구 단위로 캐싱하여 API 중복 호출 방지
     * @param areaCd 지역 코드
     * @param signguCd 시군구 코드
     * @param previousD 이전 D 값 (폴백용)
     * @return 정규화된 D 값 (0~1), 실패 시 previousD 반환
     */
    public Double collectD(String areaCd, String signguCd, Double previousD) {
        String cacheKey = areaCd + ":" + signguCd;
        
        // 캐시 확인
        if (dCache.containsKey(cacheKey)) {
            return dCache.get(cacheKey);
        }
        
        try {
            // 체류 강도 조회
            List<AreaTarDemDsDto> stayData = areaTarDemDsClient.fetchStayIntensity(areaCd, signguCd);
            Double stayValue = null;
            if (stayData != null && !stayData.isEmpty()) {
                stayValue = stayData.get(0).getStayValue();
            }

            // 소비 강도 조회
            List<AreaTarDemDsDto> spendData = areaTarDemDsClient.fetchSpendIntensity(areaCd, signguCd);
            Double spendValue = null;
            if (spendData != null && !spendData.isEmpty()) {
                spendValue = spendData.get(0).getSpendValue();
            }

            if (stayValue != null && spendValue != null) {
                double stayNormalized = NormalizationConstants.normalizeDStay(stayValue);
                double spendNormalized = NormalizationConstants.normalizeDSpend(spendValue);
                double d = (stayNormalized + spendNormalized) / 2.0;
                
                dCache.put(cacheKey, d);
                log.debug("D 지표 수집 성공: areaCd={}, signguCd={}, 체류={}, 소비={}, 정규화값={}", 
                        areaCd, signguCd, stayValue, spendValue, d);
                return d;
            }
        } catch (Exception e) {
            log.warn("D 지표 수집 실패: areaCd={}, signguCd={}, error={}", areaCd, signguCd, e.getMessage());
        }
        
        // API 실패 시 이전 값 유지 (폴백)
        log.debug("D 지표 폴백: areaCd={}, signguCd={}, 이전값={}", areaCd, signguCd, previousD);
        return previousD;
    }

    /**
     * R (지역별 관광 자원 수요) 수집
     * 시군구 단위로 캐싱하여 API 중복 호출 방지
     * @param areaCd 지역 코드
     * @param signguCd 시군구 코드
     * @param previousR 이전 R 값 (폴백용)
     * @return 정규화된 R 값 (0~1), 실패 시 previousR 반환
     */
    public Double collectR(String areaCd, String signguCd, Double previousR) {
        String cacheKey = areaCd + ":" + signguCd;
        
        // 캐시 확인
        if (rCache.containsKey(cacheKey)) {
            return rCache.get(cacheKey);
        }
        
        try {
            // 서비스 수요 조회
            List<AreaTarResDemDto> serviceData = areaTarResDemClient.fetchServiceDemand(areaCd, signguCd);
            Double serviceValue = null;
            if (serviceData != null && !serviceData.isEmpty()) {
                serviceValue = serviceData.get(0).getServiceValue();
            }

            // 문화 자원 수요 조회
            List<AreaTarResDemDto> cultureData = areaTarResDemClient.fetchCultureDemand(areaCd, signguCd);
            Double cultureValue = null;
            if (cultureData != null && !cultureData.isEmpty()) {
                cultureValue = cultureData.get(0).getCultureValue();
            }

            if (serviceValue != null && cultureValue != null) {
                double serviceNormalized = NormalizationConstants.normalizeRService(serviceValue);
                double cultureNormalized = NormalizationConstants.normalizeRCulture(cultureValue);
                double r = (serviceNormalized + cultureNormalized) / 2.0;
                
                rCache.put(cacheKey, r);
                log.debug("R 지표 수집 성공: areaCd={}, signguCd={}, 서비스={}, 문화={}, 정규화값={}", 
                        areaCd, signguCd, serviceValue, cultureValue, r);
                return r;
            }
        } catch (Exception e) {
            log.warn("R 지표 수집 실패: areaCd={}, signguCd={}, error={}", areaCd, signguCd, e.getMessage());
        }
        
        // API 실패 시 이전 값 유지 (폴백)
        log.debug("R 지표 폴백: areaCd={}, signguCd={}, 이전값={}", areaCd, signguCd, previousR);
        return previousR;
    }

    /**
     * S (전국 방문자수 빅데이터 계수) 수집
     * 광역시도 단위로 전체 배치에서 딱 한 번만 호출하여 재사용
     * @param areaCd 지역 코드
     * @return S 계수 (0.9, 1.0, 1.1, 1.2 중 하나)
     */
    public Double collectS(String areaCd) {
        // 캐시 확인
        if (sCache.containsKey(areaCd)) {
            return sCache.get(areaCd);
        }
        
        try {
            // 전달 대비 전전달 증감률 계산을 위해 60일치 데이터 필요
            LocalDate endDate = LocalDate.now().minusDays(1); // 어제
            LocalDate startDate = endDate.minusDays(59); // 60일 전
            
            List<DataLabDto> visitorData = dataLabClient.fetchVisitorCounts(areaCd, startDate, endDate);
            
            if (visitorData != null && visitorData.size() >= 30) {
                // 데이터 정렬 (날짜순)
                visitorData.sort(Comparator.comparing(DataLabDto::getBaseYmd));
                
                // 전전달 합계 (30일 전 ~ 60일 전)
                long prevPrevMonthSum = visitorData.stream()
                        .skip(30)
                        .limit(30)
                        .mapToLong(DataLabDto::getVisitorCount)
                        .filter(Objects::nonNull)
                        .sum();
                
                // 전달 합계 (0일 전 ~ 30일 전)
                long prevMonthSum = visitorData.stream()
                        .limit(30)
                        .mapToLong(DataLabDto::getVisitorCount)
                        .filter(Objects::nonNull)
                        .sum();
                
                if (prevPrevMonthSum > 0) {
                    double growthRate = ((double) prevMonthSum - prevPrevMonthSum) / prevPrevMonthSum * 100.0;
                    
                    // S 계수 결정 규칙
                    double s;
                    if (growthRate <= -5.0) {
                        s = 0.9;
                    } else if (growthRate > -5.0 && growthRate <= 5.0) {
                        s = 1.0;
                    } else if (growthRate > 5.0 && growthRate <= 15.0) {
                        s = 1.1;
                    } else {
                        s = 1.2;
                    }
                    
                    sCache.put(areaCd, s);
                    log.debug("S 계수 계산 성공: areaCd={}, 전전월={}, 전월={}, 증감률={}%, S={}", 
                            areaCd, prevPrevMonthSum, prevMonthSum, growthRate, s);
                    return s;
                }
            }
        } catch (Exception e) {
            log.warn("S 계수 수집 실패: areaCd={}, error={}", areaCd, e.getMessage());
        }
        
        // API 실패 시 기본값 1.0 반환
        log.debug("S 계수 폴백: areaCd={}, 기본값=1.0", areaCd);
        return 1.0;
    }

    /**
     * S 계수 캐시 초기화 (전체 배치 시작 전 호출)
     * 모든 광역시도의 S 계수를 미리 계산하여 캐싱
     * @param areaCodes 전체 광역시도 코드 리스트
     */
    public void initializeSCache(List<String> areaCodes) {
        if (sCacheInitialized) {
            return;
        }
        
        log.info("S 계수 캐시 초기화 시작: areaCodes={}", areaCodes);
        for (String areaCd : areaCodes) {
            collectS(areaCd);
        }
        sCacheInitialized = true;
        log.info("S 계수 캐시 초기화 완료");
    }

    /**
     * 캐시 초기화 (매일 배치 시작 전 호출)
     */
    public void clearCache() {
        dCache.clear();
        rCache.clear();
        sCache.clear();
        sCacheInitialized = false;
        log.info("지표 캐시 초기화 완료");
    }
}
