// src/main/java/com/tourfolio/app/dto/WatchlistResponse.java
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
public class WatchlistResponse {
    private Long id;
    private Long spotId;
    private String spotName;
    private String region;
    private String theme;
    private BigDecimal currentPrice;
    private BigDecimal changeRate;
    private LocalDateTime createdAt;
}
