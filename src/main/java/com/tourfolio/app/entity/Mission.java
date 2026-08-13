package com.tourfolio.app.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "missions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String category; // VISIT, COLLECT, INVEST, ATTENDANCE

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "condition_type", nullable = false, length = 30)
    private String conditionType; // CARD_COUNT, FIRST_BUY, STOCK_QTY_SINGLE, STOCK_QTY_DISTINCT, CONSECUTIVE_DAYS, CUMULATIVE_DAYS

    @Column(name = "condition_target", nullable = false)
    private Integer conditionTarget;

    @Column(name = "reward_points", nullable = false)
    private Integer rewardPoints;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}