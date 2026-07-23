package com.tourfolio.app.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
public class TatsCnctrRateDto {

    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");

    @JsonProperty("baseYmd")
    private String baseYmd;

    @JsonProperty("areaCd")
    private String areaCd;

    @JsonProperty("signguCd")
    private String signguCd;

    @JsonProperty("tAtsNm")
    private String tAtsNm;

    @JsonProperty("cnctrRate")
    private String cnctrRate;

    /** 집중률 예측값 (피크 대비 상대값 0~100) */
    public Double getPredictedValue() {
        try {
            if (cnctrRate != null && !cnctrRate.isEmpty()) {
                return Double.parseDouble(cnctrRate);
            }
        } catch (NumberFormatException e) {
            // 파싱 불가 시 null 처리하여 상위에서 건너뛰게 한다
        }
        return null;
    }

    /** 예측 대상 일자 */
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
