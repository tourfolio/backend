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
    private String imageUrl;
    private String mapX;
    private String mapY;
}