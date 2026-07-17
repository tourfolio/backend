// src/main/java/com/tourfolio/app/dto/PriceHistoryResponse.java
package com.tourfolio.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistoryResponse {
    private LocalDate tradeDate;
    private BigDecimal price;
    private BigDecimal changeRate;
    private BigDecimal tsScore;
}
