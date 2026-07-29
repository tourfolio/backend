package com.tourfolio.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_cards", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "card_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "card_id", nullable = false)
    private Long cardId;

    @Column(name = "acquired_at", nullable = false)
    private LocalDateTime acquiredAt;

    @Column(name = "acquisition_path", length = 50)
    private String acquisitionPath;

    @PrePersist
    public void prePersist() {
        if (this.acquiredAt == null) {
            this.acquiredAt = LocalDateTime.now();
        }
        if (this.acquisitionPath == null) {
            this.acquisitionPath = "관광지 방문";
        }
    }
}
