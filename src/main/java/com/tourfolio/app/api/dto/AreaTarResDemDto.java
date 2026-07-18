package com.tourfolio.app.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * R (지역별 관광 자원 수요) API 응답 DTO
 * API 1: GET https://apis.data.go.kr/B551011/AreaTarResDemService/areaTarSvcDemList (관광 서비스 수요)
 * API 2: GET https://apis.data.go.kr/B551011/AreaTarResDemService/areaCulResDemList (문화 자원 수요)
 */
@Data
public class AreaTarResDemDto {
    @JsonProperty("areaCd")
    private String areaCd;

    @JsonProperty("signguCd")
    private String signguCd;

    @JsonProperty("tarSvcDemIxCd")
    private String tarSvcDemIxCd;

    @JsonProperty("culResDemIxCd")
    private String culResDemIxCd;

    @JsonProperty("tarSvcDemIxVal")
    private String tarSvcDemIxVal;

    @JsonProperty("culResDemIxVal")
    private String culResDemIxVal;

    @JsonProperty("baseYm")
    private String baseYm;

    public Double getServiceValue() {
        try {
            if (tarSvcDemIxVal != null && !tarSvcDemIxVal.isEmpty()) {
                return Double.parseDouble(tarSvcDemIxVal);
            }
        } catch (NumberFormatException e) {
            // ignore
        }
        return null;
    }

    public Double getCultureValue() {
        try {
            if (culResDemIxVal != null && !culResDemIxVal.isEmpty()) {
                return Double.parseDouble(culResDemIxVal);
            }
        } catch (NumberFormatException e) {
            // ignore
        }
        return null;
    }
}
