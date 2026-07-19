// src/main/java/com/tourfolio/app/service/OpenApiService.java
package com.tourfolio.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;

@Service
@Slf4j
public class OpenApiService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String SERVICE_KEY = "9gW0r4tl6Md49Aj9LDANE3in2AU8Flq7n%2BR%2F4GTTeYn5lukUig9L8VpQprv%2B3XNhbLJmtGkOYtydXle35DQG8A%3D%3D";

    // 한국관광공사 API 엔드포인트
    private static final String API_BASE_URL = "http://api.visitkorea.or.kr/openapi/service/rest/KorService";
    private static final String AREA_BASED_LIST_URL = API_BASE_URL + "/areaBasedList";
    private static final String DETAIL_COMMON_URL = API_BASE_URL + "/detailCommon";

    // 정규화 기준값 상수 (검수서 기준)
    private static final BigDecimal P_MIN = BigDecimal.valueOf(8.31);
    private static final BigDecimal P_MAX = BigDecimal.valueOf(100.0);
    private static final BigDecimal P_RANGE = BigDecimal.valueOf(91.69);

    private static final BigDecimal D_STAY_MIN = BigDecimal.valueOf(60.46);
    private static final BigDecimal D_STAY_MAX = BigDecimal.valueOf(125.2);
    private static final BigDecimal D_STAY_RANGE = BigDecimal.valueOf(64.74);

    private static final BigDecimal D_SPEND_MIN = BigDecimal.valueOf(54.09);
    private static final BigDecimal D_SPEND_MAX = BigDecimal.valueOf(147.02);
    private static final BigDecimal D_SPEND_RANGE = BigDecimal.valueOf(92.93);

    private static final BigDecimal R_SERVICE_MIN = BigDecimal.valueOf(63.55);
    private static final BigDecimal R_SERVICE_MAX = BigDecimal.valueOf(152.49);
    private static final BigDecimal R_SERVICE_RANGE = BigDecimal.valueOf(88.94);

    private static final BigDecimal R_CULTURE_MIN = BigDecimal.valueOf(64.24);
    private static final BigDecimal R_CULTURE_MAX = BigDecimal.valueOf(136.21);
    private static final BigDecimal R_CULTURE_RANGE = BigDecimal.valueOf(71.97);

    public String fetchTourismData(String apiUrl, String areaCode) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                    .queryParam("serviceKey", SERVICE_KEY)
                    .queryParam("areaCode", areaCode)
                    .queryParam("_type", "json")
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUriString();

            log.info("한국관광공사 API 호출: {}", url);
            String response = restTemplate.getForObject(url, String.class);
            log.info("API 응답 수신 완료");
            return response;
        } catch (Exception e) {
            log.error("한국관광공사 API 호출 실패: {}", e.getMessage());
            throw new RuntimeException("관광 데이터 조회 실패", e);
        }
    }

    public String fetchAreaBasedList(String areaCode, int pageNo, int numOfRows) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(AREA_BASED_LIST_URL)
                    .queryParam("serviceKey", SERVICE_KEY)
                    .queryParam("areaCode", areaCode)
                    .queryParam("pageNo", pageNo)
                    .queryParam("numOfRows", numOfRows)
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "Tourfolio")
                    .queryParam("_type", "json")
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUriString();

            log.info("지역기반 관광지 목록 조회: areaCode={}, pageNo={}", areaCode, pageNo);
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            log.error("지역기반 관광지 목록 조회 실패: {}", e.getMessage());
            throw new RuntimeException("관광지 목록 조회 실패", e);
        }
    }

    public BigDecimal calculateFallbackTourismScore(String theme, String region) {
        log.warn("기본 점수 계산 메서드 호출됨: theme={}, region={}", theme, region);
        return BigDecimal.ZERO;
    }

    public String fetchDetailInfo(String contentId) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(DETAIL_COMMON_URL)
                    .queryParam("serviceKey", SERVICE_KEY)
                    .queryParam("contentId", contentId)
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "Tourfolio")
                    .queryParam("defaultYN", "Y")
                    .queryParam("firstImageYN", "Y")
                    .queryParam("areacodeYN", "Y")
                    .queryParam("catcodeYN", "Y")
                    .queryParam("addrinfoYN", "Y")
                    .queryParam("mapinfoYN", "Y")
                    .queryParam("overviewYN", "Y")
                    .queryParam("_type", "json")
                    .build()
                    .encode(StandardCharsets.UTF_8)
                    .toUriString();

            log.info("상세 정보 조회: contentId={}", contentId);
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            log.error("상세 정보 조회 실패: {}", e.getMessage());
            throw new RuntimeException("상세 정보 조회 실패", e);
        }
    }

    // P(인기) 지표 정규화: (값 - 8.31) / 91.69
    public BigDecimal normalizePopularity(BigDecimal rawValue) {
        if (rawValue == null) {
            log.warn("인기 지표 원본 값이 null이어서 기본값 0 사용");
            return BigDecimal.ZERO;
        }
        BigDecimal normalized = rawValue.subtract(P_MIN).divide(P_RANGE, 4, java.math.RoundingMode.HALF_UP);
        log.debug("인기 지표 정규화: 원본={}, 정규화={}", rawValue, normalized);
        return normalized;
    }

    // D(체류) 지표 정규화: (값 - 60.46) / 64.74
    public BigDecimal normalizeStayDuration(BigDecimal rawValue) {
        if (rawValue == null) {
            log.warn("체류 시간 원본 값이 null이어서 기본값 0 사용");
            return BigDecimal.ZERO;
        }
        BigDecimal normalized = rawValue.subtract(D_STAY_MIN).divide(D_STAY_RANGE, 4, java.math.RoundingMode.HALF_UP);
        log.debug("체류 시간 정규화: 원본={}, 정규화={}", rawValue, normalized);
        return normalized;
    }

    // D(소비) 지표 정규화: (값 - 54.09) / 92.93
    public BigDecimal normalizeSpending(BigDecimal rawValue) {
        if (rawValue == null) {
            log.warn("소비 금액 원본 값이 null이어서 기본값 0 사용");
            return BigDecimal.ZERO;
        }
        BigDecimal normalized = rawValue.subtract(D_SPEND_MIN).divide(D_SPEND_RANGE, 4, java.math.RoundingMode.HALF_UP);
        log.debug("소비 금액 정규화: 원본={}, 정규화={}", rawValue, normalized);
        return normalized;
    }

    // R(서비스) 지표 정규화: (값 - 63.55) / 88.94
    public BigDecimal normalizeServiceQuality(BigDecimal rawValue) {
        if (rawValue == null) {
            log.warn("서비스 품질 원본 값이 null이어서 기본값 0 사용");
            return BigDecimal.ZERO;
        }
        BigDecimal normalized = rawValue.subtract(R_SERVICE_MIN).divide(R_SERVICE_RANGE, 4, java.math.RoundingMode.HALF_UP);
        log.debug("서비스 품질 정규화: 원본={}, 정규화={}", rawValue, normalized);
        return normalized;
    }

    // R(문화) 지표 정규화: (값 - 64.24) / 71.97
    public BigDecimal normalizeCultureIndex(BigDecimal rawValue) {
        if (rawValue == null) {
            log.warn("문화 지수 원본 값이 null이어서 기본값 0 사용");
            return BigDecimal.ZERO;
        }
        BigDecimal normalized = rawValue.subtract(R_CULTURE_MIN).divide(R_CULTURE_RANGE, 4, java.math.RoundingMode.HALF_UP);
        log.debug("문화 지수 정규화: 원본={}, 정규화={}", rawValue, normalized);
        return normalized;
    }

    // 관광지 가격 계산 공식: P × 0.60 + D × 0.25 + R × 0.15
    public BigDecimal calculateTourismScore(BigDecimal pNormalized, BigDecimal dNormalized, BigDecimal rNormalized) {
        BigDecimal score = pNormalized.multiply(BigDecimal.valueOf(0.60))
                .add(dNormalized.multiply(BigDecimal.valueOf(0.25)))
                .add(rNormalized.multiply(BigDecimal.valueOf(0.15)))
                .setScale(4, java.math.RoundingMode.HALF_UP);
        log.debug("관광지 점수 계산: P={}, D={}, R={}, 결과={}", pNormalized, dNormalized, rNormalized, score);
        return score;
    }

    // 상하한가 ±10% Clamp 로직
    public BigDecimal applyPriceLimit10Percent(BigDecimal changeRate) {
        BigDecimal maxChange = BigDecimal.valueOf(0.10);
        BigDecimal minChange = BigDecimal.valueOf(-0.10);
        
        if (changeRate.compareTo(maxChange) > 0) {
            log.debug("상한가 10% 도달로 변동률 제한: 원본={}, 제한={}", changeRate, maxChange);
            return maxChange;
        }
        if (changeRate.compareTo(minChange) < 0) {
            log.debug("하한가 10% 도달로 변동률 제한: 원본={}, 제한={}", changeRate, minChange);
            return minChange;
        }
        return changeRate;
    }

    // 요일 기반 S 계수 로직 (향후 빅데이터 API 확장 가능)
    public BigDecimal calculateDayOfWeekCoefficient() {
        int dayOfWeek = java.time.LocalDateTime.now().getDayOfWeek().getValue();
        BigDecimal coefficient;
        
        switch (dayOfWeek) {
            case 6: // 토요일
            case 7: // 일요일
                coefficient = BigDecimal.valueOf(1.1);
                break;
            case 5: // 금요일
                coefficient = BigDecimal.valueOf(1.2);
                break;
            case 1: // 월요일
            case 2: // 화요일
                coefficient = BigDecimal.valueOf(0.9);
                break;
            default: // 수요일, 목요일
                coefficient = BigDecimal.valueOf(1.0);
                break;
        }
        
        log.debug("요일 기반 계수: 요일={}, 계수={}", dayOfWeek, coefficient);
        return coefficient;
    }

    // API 응답에서 JSON 파싱 헬퍼 메서드
    public JsonNode parseJsonResponse(String jsonResponse) {
        try {
            return objectMapper.readTree(jsonResponse);
        } catch (Exception e) {
            log.error("JSON 파싱 실패: {}", e.getMessage());
            throw new RuntimeException("JSON 파싱 실패", e);
        }
    }

    // API 응답에서 아이템 추출 헬퍼 메서드
    public JsonNode extractItems(JsonNode rootNode) {
        JsonNode response = rootNode.path("response");
        JsonNode body = response.path("body");
        JsonNode items = body.path("items");
        
        if (items.isArray()) {
            return items;
        }
        
        JsonNode item = items.path("item");
        return item.isArray() ? item : objectMapper.createArrayNode().add(item);
    }

}