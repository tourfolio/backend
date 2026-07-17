// src/main/java/com/tourfolio/app/dto/RegionalIndexResponse.java
package com.tourfolio.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegionalIndexResponse {
    private String region;
    private BigDecimal averageChangeRate;
    private Long spotCount;
}
