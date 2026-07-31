// src/main/java/com/tourfolio/app/entity/Spot.java

package com.tourfolio.app.entity;



import jakarta.persistence.*;

import lombok.AllArgsConstructor;

import lombok.Builder;

import lombok.Data;

import lombok.NoArgsConstructor;



import java.math.BigDecimal;

import java.time.LocalDateTime;



@Entity

@Table(name = "spots")

@Data

@Builder

@NoArgsConstructor

@AllArgsConstructor

public class Spot {



    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;



    @Column(nullable = false, length = 100)

    private String name;



    @Column(nullable = false, length = 20)

    private String areaCode;



    @Column(nullable = false, length = 50)

    private String contentId;



    @Column(length = 20)

    private String signguCd;



    @Column(nullable = false)

    private Integer tier;



    @Column(nullable = false, length = 20)

    private String theme;



    @Column(nullable = false, length = 50)

    private String region;



    @Column(length = 255)

    private String address;



    @Column(name = "initial_price", nullable = false, precision = 19, scale = 2)

    private BigDecimal initialPrice;



    @Column(name = "ipo_price", precision = 19, scale = 2)

    private BigDecimal ipoPrice;



    @Column(name = "current_price", nullable = false, precision = 19, scale = 2)

    private BigDecimal currentPrice;



    @Column(name = "prev_price", nullable = false, precision = 19, scale = 2)

    private BigDecimal prevPrice;



    @Column(name = "tourism_data_weight", nullable = false, precision = 19, scale = 2)

    private BigDecimal tourismDataWeight;



    @Column(name = "last_updated", nullable = false)

    private LocalDateTime lastUpdated;



    @Column(name = "created_at", nullable = false)

    private LocalDateTime createdAt;



    @Column(length = 500)

    private String imageUrl;



    @Column(length = 50)

    private String areaName;



    @Column(length = 50)

    private String themeTag;



    @Column(length = 500)

    private String description;



    @Column(name = "view_count")

    @Builder.Default

    private Long viewCount = 0L;

}