package com.tourfolio.app.api.client;

import com.tourfolio.app.api.PublicApiResponse;
import com.tourfolio.app.api.dto.DataLabDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * S (전국 방문자수 빅데이터) API 클라이언트
 * API: GET https://apis.data.go.kr/B551011/DataLabService/metcoRegnVisitrDDList
 * (광역 지자체 지역방문자수, touDivCd=2 외지인만 사용)
 */
@Component
public class DataLabClient {

    private final WebClient webClient;
    private final String serviceKey;
    private final String baseUrl;

    public DataLabClient(
            WebClient.Builder webClientBuilder,
            @Value("${tour.api.service-key}") String serviceKey,
            @Value("${tour.api.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.build();
        this.serviceKey = serviceKey;
        this.baseUrl = baseUrl;
    }

    /**
     * 광역 지자체 지역방문자수 데이터 조회 (외지인만)
     * @param areaCd 지역 코드
     * @param startDate 조회 시작일 (30일 이전 데이터만 사용 가능)
     * @param endDate 조회 종료일
     * @return 방문자수 데이터 리스트
     */
    public List<DataLabDto> fetchVisitorCounts(String areaCd, LocalDate startDate, LocalDate endDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
            
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/B551011/DataLabService/metcoRegnVisitrDDList")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("pageNo", "1")
                    .queryParam("numOfRows", "31")
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "Tourfolio")
                    .queryParam("areaCd", areaCd)
                    .queryParam("touDivCd", "2")
                    .queryParam("stdYmd", startDate.format(formatter))
                    .queryParam("endYmd", endDate.format(formatter))
                    .queryParam("_type", "json")
                    .build(true)
                    .toUriString();

            PublicApiResponse<DataLabDto> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<PublicApiResponse<DataLabDto>>() {})
                    .block();

            if (response != null && response.isSuccess()) {
                return response.getItems();
            }
        } catch (Exception e) {
            // API 실패 시 null 반환
        }
        return List.of();
    }
}
