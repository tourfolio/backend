package com.tourfolio.app.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "point_histories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 20)
    private String type; // SIGNUP, ATTENDANCE, MISSION, STOCK_BUY, STOCK_SELL

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private Long amount; // 증가 양수, 차감 음수

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}