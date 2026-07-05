package com.tourfolio.app.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.nio.charset.StandardCharsets;

@Service
public class OpenApiService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String SERVICE_KEY = "9gW0r4tl6Md49Aj9LDANE3in2AU8Flq7n%2BR%2F4GTTeYn5lukUig9L8VpQprv%2B3XNhbLJmtGkOYtydXle35DQG8A%3D%3D"; // 본인의 인증키 입력

    public String fetchTourismData(String apiUrl, String areaCode) {
        // UTF-8 인코딩을 강제하여 한글 깨짐 원천 차단
        String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .queryParam("serviceKey", SERVICE_KEY)
                .queryParam("areaCode", areaCode)
                .queryParam("_type", "json")
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();

        return restTemplate.getForObject(url, String.class);
    }
}