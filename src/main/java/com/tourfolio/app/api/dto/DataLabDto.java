package com.tourfolio.app.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * S (전국 방문자수 빅데이터) API 응답 DTO
 * API: GET https://apis.data.go.kr/B551011/DataLabService/metcoRegnVisitrDDList
 * (광역 지자체 지역방문자수, touDivCd=2 외지인만 사용)
 */
@Data
public class DataLabDto {
    @JsonProperty("areaCd")
    private String areaCd;

    @JsonProperty("areaNm")
    private String areaNm;

    @JsonProperty("touDivCd")
    private String touDivCd;

    @JsonProperty("visitrCnt")
    private String visitrCnt;

    @JsonProperty("baseYmd")
    private String baseYmd;

    public Long getVisitorCount() {
        try {
            if (visitrCnt != null && !visitrCnt.isEmpty()) {
                return Long.parseLong(visitrCnt);
            }
        } catch (NumberFormatException e) {
            // ignore
        }
        return null;
    }
}
