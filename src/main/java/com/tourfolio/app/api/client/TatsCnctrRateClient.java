package com.tourfolio.app.api.client;

import com.tourfolio.app.api.PublicApiResponse;
import com.tourfolio.app.api.dto.TatsCnctrRateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
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
                    // 향후 30일치 예측을 모두 받아온 뒤 앞 7일만 사용한다
                    .queryParam("numOfRows", "40")
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "Tourfolio")
                    .queryParam("areaCd", areaCd)
                    .queryParam("signguCd", signguCd)
                    // build(true)는 인코딩을 건너뛰므로 한글 관광지명은 직접 인코딩해야 한다
                    .queryParam("tAtsNm", URLEncoder.encode(tAtsNm, StandardCharsets.UTF_8))
                    .queryParam("_type", "json")
                    .build(true)
                    .toUriString();

            PublicApiResponse<TatsCnctrRateDto> response = webClient.get()
                    .uri(java.net.URI.create(url))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<PublicApiResponse<TatsCnctrRateDto>>() {})
                    .block();

            if (response != null && response.isSuccess()) {
                return response.getItems();
            }
            log.warn("P 지표 응답 실패: spot={} resultCode={}", tAtsNm, PublicApiResponse.resultCodeOf(response));
        } catch (Exception e) {
            log.warn("P 지표 호출 실패: spot={} error={}", tAtsNm, e.toString());
        }
        return List.of();
    }
}
