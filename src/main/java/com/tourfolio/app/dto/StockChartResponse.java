// src/main/java/com/tourfolio/app/dto/StockChartResponse.java
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
public class StockChartResponse {
    private LocalDate date;
    private BigDecimal price;
}
