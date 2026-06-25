package com.tourfolio.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolios")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long spotId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal averagePurchasePrice;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}