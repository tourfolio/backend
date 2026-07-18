package com.tourfolio.app.api.client;

import com.tourfolio.app.api.PublicApiResponse;
import com.tourfolio.app.api.dto.AreaTarDemDsDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/**
 * D (지역별 관광 수요 강도) API 클라이언트
 * API 1: GET https://apis.data.go.kr/B551011/AreaTarDemDsService/areaTarSjrnDsList (관광 체류 강도)
 * API 2: GET https://apis.data.go.kr/B551011/AreaTarDemDsService/areaTarExpDsList (관광 소비 강도)
 */
@Component
public class AreaTarDemDsClient {

    private final WebClient webClient;
    private final String serviceKey;
    private final String baseUrl;

    public AreaTarDemDsClient(
            WebClient.Builder webClientBuilder,
            @Value("${tour.api.service-key}") String serviceKey,
            @Value("${tour.api.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.build();
        this.serviceKey = serviceKey;
        this.baseUrl = baseUrl;
    }

    /**
     * 관광 체류 강도 데이터 조회 (tarSjrnDsIxCd=21 전체 사용)
     * @param areaCd 지역 코드
     * @param signguCd 시군구 코드
     * @return 체류 강도 데이터 리스트
     */
    public List<AreaTarDemDsDto> fetchStayIntensity(String areaCd, String signguCd) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/B551011/AreaTarDemDsService/areaTarSjrnDsList")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("pageNo", "1")
                    .queryParam("numOfRows", "10")
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "Tourfolio")
                    .queryParam("areaCd", areaCd)
                    .queryParam("signguCd", signguCd)
                    .queryParam("tarSjrnDsIxCd", "21")
                    .queryParam("_type", "json")
                    .build(true)
                    .toUriString();

            PublicApiResponse<AreaTarDemDsDto> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<PublicApiResponse<AreaTarDemDsDto>>() {})
                    .block();

            if (response != null && response.isSuccess()) {
                return response.getItems();
            }
        } catch (Exception e) {
            // API 실패 시 null 반환
        }
        return List.of();
    }

    /**
     * 관광 소비 강도 데이터 조회 (tarExpDsIxCd=22 전체 사용)
     * @param areaCd 지역 코드
     * @param signguCd 시군구 코드
     * @return 소비 강도 데이터 리스트
     */
    public List<AreaTarDemDsDto> fetchSpendIntensity(String areaCd, String signguCd) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/B551011/AreaTarDemDsService/areaTarExpDsList")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("pageNo", "1")
                    .queryParam("numOfRows", "10")
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "Tourfolio")
                    .queryParam("areaCd", areaCd)
                    .queryParam("signguCd", signguCd)
                    .queryParam("tarExpDsIxCd", "22")
                    .queryParam("_type", "json")
                    .build(true)
                    .toUriString();

            PublicApiResponse<AreaTarDemDsDto> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<PublicApiResponse<AreaTarDemDsDto>>() {})
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
