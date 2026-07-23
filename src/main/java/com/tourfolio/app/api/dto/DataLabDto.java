package com.tourfolio.app.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


@Data
public class DataLabDto {

    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** 주의: 다른 API와 달리 필드명이 areaCd가 아니라 areaCode 이다 */
    @JsonProperty("areaCode")
    private String areaCode;

    @JsonProperty("areaNm")
    private String areaNm;

    @JsonProperty("touDivCd")
    private String touDivCd;

    /** 방문자수. "1823559.5" 처럼 소수점이 붙어 내려오므로 Double로 파싱한다 */
    @JsonProperty("touNum")
    private String touNum;

    @JsonProperty("baseYmd")
    private String baseYmd;

    public Double getVisitorCount() {
        try {
            if (touNum != null && !touNum.isEmpty()) {
                return Double.parseDouble(touNum);
            }
        } catch (NumberFormatException e) {
            // 파싱 불가 시 null
        }
        return null;
    }

    /** 외지인 여부 (S 계수는 외지인만 사용) */
    public boolean isOutsider() {
        return "2".equals(touDivCd);
    }

    public LocalDate getBaseDate() {
        try {
            if (baseYmd != null && baseYmd.length() == 8) {
                return LocalDate.parse(baseYmd, YMD);
            }
        } catch (Exception e) {
            // 파싱 불가 시 null
        }
        return null;
    }
}
