package com.tourfolio.app.api.client;

import com.tourfolio.app.api.PublicApiResponse;
import com.tourfolio.app.api.dto.TatsCnctrRateDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/**
 * P (관광지 집중률 예측) API 클라이언트
 * API: GET https://apis.data.go.kr/B551011/TatsCnctrRateService/tatsCnctrRatedList
 */
@Component
public class TatsCnctrRateClient {

    private final WebClient webClient;
    private final String serviceKey;
    private final String baseUrl;

    public TatsCnctrRateClient(
            WebClient.Builder webClientBuilder,
            @Value("${tour.api.service-key}") String serviceKey,
            @Value("${tour.api.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.build();
        this.serviceKey = serviceKey;
        this.baseUrl = baseUrl;
    }

    /**
     * 관광지 집중률 예측 데이터 조회
     * @param areaCd 지역 코드
     * @param signguCd 시군구 코드
     * @param tAtsNm 관광지 이름
     * @return 예측 데이터 리스트
     */
    public List<TatsCnctrRateDto> fetchPredictions(String areaCd, String signguCd, String tAtsNm) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/B551011/TatsCnctrRateService/tatsCnctrRatedList")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("pageNo", "1")
                    .queryParam("numOfRows", "10")
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "Tourfolio")
                    .queryParam("areaCd", areaCd)
                    .queryParam("signguCd", signguCd)
                    .queryParam("tAtsNm", tAtsNm)
                    .queryParam("_type", "json")
                    .build(true)
                    .toUriString();

            PublicApiResponse<TatsCnctrRateDto> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<PublicApiResponse<TatsCnctrRateDto>>() {})
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
