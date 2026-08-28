package com.tourfolio.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockResponse {
    private Long id;
    private String name;
    private String areaCode;
    private String regionName;
    private Integer tier;
    private BigDecimal currentPrice;
    private BigDecimal prevPrice;
    private BigDecimal changeRate;
    private LocalDateTime lastUpdated;
    private String address;

    private BigDecimal todayTradeVolume;   // 오늘 거래량 (전체 유저 매수+매도 합산)
    private BigDecimal visitorForecast;    // 방문자 예측 (P, 0~1)
    private BigDecimal demandIntensity;    // 수요 강도 (D, 0~1)
    private BigDecimal resourceDemand;     // 자원 수요 (R, 0~1)
}