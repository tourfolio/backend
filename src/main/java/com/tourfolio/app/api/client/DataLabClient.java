package com.tourfolio.app.api.client;

import com.tourfolio.app.api.PublicApiResponse;
import com.tourfolio.app.api.dto.DataLabDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
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
     * 전국 광역 지자체 지역방문자수 데이터 조회.
     *
     * 이 API는 areaCd / touDivCd 를 요청 파라미터로 받지 않는다(넘기면 오히려 오류).
     * 전국 17개 시도 × 3개 관광객 구분이 일자별로 모두 내려오므로,
     * 외지인(touDivCd=2) 선별과 전국 합산은 호출한 쪽에서 수행한다.
     *
     * @param startDate 조회 시작일 (데이터는 약 30일 지연 발행)
     * @param endDate 조회 종료일
     * @return 방문자수 데이터 리스트 (전 지역/전 구분 혼재)
     */
    public List<DataLabDto> fetchVisitorCounts(LocalDate startDate, LocalDate endDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

            // 하루 51행(17개 시도 × 3개 구분)이므로 여유 있게 요청한다
            long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            long rows = Math.max(days, 1) * 51 + 100;

            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/B551011/DataLabService/metcoRegnVisitrDDList")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("pageNo", "1")
                    .queryParam("numOfRows", String.valueOf(rows))
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "Tourfolio")
                    .queryParam("startYmd", startDate.format(formatter))
                    .queryParam("endYmd", endDate.format(formatter))
                    .queryParam("_type", "json")
                    .build(true)
                    .toUriString();

            PublicApiResponse<DataLabDto> response = webClient.get()
                    .uri(java.net.URI.create(url))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<PublicApiResponse<DataLabDto>>() {})
                    .block();

            if (response != null && response.isSuccess()) {
                return response.getItems();
            }
            log.warn("S 지표 응답 실패: {}~{} resultCode={}", startDate, endDate, PublicApiResponse.resultCodeOf(response));
        } catch (Exception e) {
            log.warn("S 지표 호출 실패: {}~{} error={}", startDate, endDate, e.toString());
        }
        return List.of();
    }
}
