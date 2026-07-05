package com.tourfolio.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class ResourceDemandService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openapi.service.key}")
    private String openApiServiceKey;

    @Value("${endpoint.resdem}")
    private String endpointResdem;

    private static final int UPDATE_DAY = 16;
    private static final int UPDATE_WINDOW_DAYS = 3;

    // 동시성 문제를 방지하기 위해 ConcurrentHashMap 사용
    private final Map<Long, CachedResourceData> cachedData = new ConcurrentHashMap<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CachedResourceData {
        private double serviceDemandScore;
        private double resourceDemandScore;
        private double finalResourceScore;
        private LocalDateTime cachedAt;
        private LocalDate dataMonth;
    }

    private boolean isUpdateGapPeriod() {
        LocalDate today = LocalDate.now();
        int currentDay = today.getDayOfMonth();
        return currentDay >= (UPDATE_DAY - UPDATE_WINDOW_DAYS) &&
                currentDay <= (UPDATE_DAY + UPDATE_WINDOW_DAYS);
    }

    private boolean isDataValid(CachedResourceData cached) {
        if (cached == null) return false;
        return cached.getDataMonth().equals(LocalDate.now().withDayOfMonth(1));
    }

    public double calculateFinalResourceScore(Long spotId, String contentId) {
        CachedResourceData cached = cachedData.get(spotId);

        if (!isUpdateGapPeriod() || !isDataValid(cached)) {
            try {
                return fetchAndCalculateNewScore(spotId, contentId);
            } catch (Exception e) {
                log.error("Failed to fetch new data for spot {}, using cached data: {}", spotId, e.getMessage());
                return (cached != null) ? cached.getFinalResourceScore() : 50.0;
            }
        }
        return cached.getFinalResourceScore();
    }

    private double fetchAndCalculateNewScore(Long spotId, String contentId) {
        double serviceDemandScore = fetchServiceDemandScore(contentId);
        double resourceDemandScore = fetchResourceDemandScore(contentId);
        double finalScore = (serviceDemandScore * 0.60) + (resourceDemandScore * 0.40);

        cachedData.put(spotId, new CachedResourceData(
                serviceDemandScore, resourceDemandScore, finalScore, LocalDateTime.now(), LocalDate.now().withDayOfMonth(1)
        ));

        return finalScore;
    }

    private double fetchServiceDemandScore(String contentId) {
        // 실제 API 호출 로직 (생략된 부분은 기존처럼 구현)
        return 75.0;
    }

    private double fetchResourceDemandScore(String contentId) {
        // 실제 API 호출 로직 (생략된 부분은 기존처럼 구현)
        return 70.0;
    }

    private double normalizeTo100(double value, double min, double max) {
        if (max == min) return 50.0;
        return Math.max(0, Math.min(100, (value - min) / (max - min) * 100.0));
    }
}