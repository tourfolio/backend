package com.tourfolio.app.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * D (지역별 관광 수요 강도) API 응답 DTO
 * API 1: GET https://apis.data.go.kr/B551011/AreaTarDemDsService/areaTarSjrnDsList (관광 체류 강도)
 * API 2: GET https://apis.data.go.kr/B551011/AreaTarDemDsService/areaTarExpDsList (관광 소비 강도)
 */
@Data
public class AreaTarDemDsDto {
    @JsonProperty("areaCd")
    private String areaCd;

    @JsonProperty("signguCd")
    private String signguCd;

    @JsonProperty("tarSjrnDsIxCd")
    private String tarSjrnDsIxCd;

    @JsonProperty("tarExpDsIxCd")
    private String tarExpDsIxCd;

    @JsonProperty("tarSjrnDsIxVal")
    private String tarSjrnDsIxVal;

    @JsonProperty("tarExpDsIxVal")
    private String tarExpDsIxVal;

    @JsonProperty("baseYm")
    private String baseYm;

    public Double getStayValue() {
        try {
            if (tarSjrnDsIxVal != null && !tarSjrnDsIxVal.isEmpty()) {
                return Double.parseDouble(tarSjrnDsIxVal);
            }
        } catch (NumberFormatException e) {
            // ignore
        }
        return null;
    }

    public Double getSpendValue() {
        try {
            if (tarExpDsIxVal != null && !tarExpDsIxVal.isEmpty()) {
                return Double.parseDouble(tarExpDsIxVal);
            }
        } catch (NumberFormatException e) {
            // ignore
        }
        return null;
    }
}
