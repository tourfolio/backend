package com.tourfolio.app.api.client;

import com.tourfolio.app.api.PublicApiResponse;
import com.tourfolio.app.api.dto.KorService2Dto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Slf4j
@Component
public class KorService2Client {

    private final WebClient webClient;
    private final String serviceKey;
    private final String baseUrl;

    public KorService2Client(
            WebClient.Builder webClientBuilder,
            @Value("${tour.api.service-key}") String serviceKey,
            @Value("${tour.api.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.build();
        this.serviceKey = serviceKey;
        this.baseUrl = baseUrl;
    }

    /**
     * 관광지 상세 정보 조회 (대표 이미지, GPS 좌표)
     * @param contentId 관광지 ID
     * @return 상세 정보
     */
    public KorService2Dto fetchDetailCommon(String contentId) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/B551011/KorService2/detailCommon2")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "tourfolio")
                    .queryParam("contentId", contentId)
                    .queryParam("defaultYN", "Y")
                    .queryParam("firstImageYN", "Y")
                    .queryParam("areacodeYN", "Y")
                    .queryParam("catcodeYN", "Y")
                    .queryParam("addrinfoYN", "Y")
                    .queryParam("mapinfoYN", "Y")
                    .queryParam("overviewYN", "N")
                    .queryParam("_type", "json")
                    .build(true)
                    .toUriString();

            PublicApiResponse<KorService2Dto> response = webClient.get()
                    .uri(java.net.URI.create(url))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<PublicApiResponse<KorService2Dto>>() {})
                    .block();

            if (response != null && response.isSuccess()) {
                List<KorService2Dto> items = response.getItems();
                if (items != null && !items.isEmpty()) {
                    return items.get(0);
                }
            }
            log.warn("KorService2 응답 실패: contentId={} resultCode={}", contentId, PublicApiResponse.resultCodeOf(response));
        } catch (Exception e) {
            log.warn("KorService2 호출 실패: contentId={} error={}", contentId, e.toString());
        }
        return null;
    }
}
