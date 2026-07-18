package com.tourfolio.app.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * P (관광지 집중률 예측) API 응답 DTO
 * API: GET https://apis.data.go.kr/B551011/TatsCnctrRateService/tatsCnctrRatedList
 */
@Data
public class TatsCnctrRateDto {
    @JsonProperty("tAtsNm")
    private String tAtsNm;

    @JsonProperty("areaCd")
    private String areaCd;

    @JsonProperty("signguCd")
    private String signguCd;

    @JsonProperty("tAtsCnctrRate")
    private String tAtsCnctrRate;

    @JsonProperty("baseYm")
    private String baseYm;

    @JsonProperty("tAtsCnctrRate30")
    private String tAtsCnctrRate30;

    public Double getPredictedValue() {
        try {
            if (tAtsCnctrRate != null && !tAtsCnctrRate.isEmpty()) {
                return Double.parseDouble(tAtsCnctrRate);
            }
        } catch (NumberFormatException e) {
            // ignore
        }
        return null;
    }
}
