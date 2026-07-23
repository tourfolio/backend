// src/main/java/com/tourfolio/app/entity/PriceHistory.java
package com.tourfolio.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "spot_id", nullable = false)
    private Long spotId;

    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "change_rate", nullable = false, precision = 10, scale = 4)
    private BigDecimal changeRate;

    @Column(name = "ts_score", precision = 10, scale = 4)
    private BigDecimal tsScore;

    // 개별 지표 보존: API 실패 시 "이전 값 유지" 폴백의 소스가 된다
    @Column(name = "p_score", precision = 10, scale = 4)
    private BigDecimal pScore;

    @Column(name = "d_score", precision = 10, scale = 4)
    private BigDecimal dScore;

    @Column(name = "r_score", precision = 10, scale = 4)
    private BigDecimal rScore;

    @Column(name = "s_coefficient", precision = 10, scale = 4)
    private BigDecimal sCoefficient;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id", insertable = false, updatable = false)
    private Spot spot;
}
