// src/main/java/com/tourfolio/app/entity/StockSpot.java
package com.tourfolio.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_spots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "spot_id")
    private Long spotId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "region_name", length = 50)
    private String regionName;

    @Column(name = "area_code", length = 20)
    private String areaCode;

    @Column(nullable = false)
    private Integer tier;

    @Column(length = 20)
    private String theme;

    @Column(name = "theme_tag", length = 50)
    private String themeTag;

    @Column(name = "initial_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal initialPrice;

    @Column(name = "current_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal currentPrice;

    @Column(name = "prev_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal prevPrice;

    @Column(name = "change_rate", precision = 19, scale = 4)
    private BigDecimal changeRate;

    @Column(name = "tourism_data_weight", nullable = false, precision = 19, scale = 2)
    private BigDecimal tourismDataWeight;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
