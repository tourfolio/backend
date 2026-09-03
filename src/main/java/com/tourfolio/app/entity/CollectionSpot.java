package com.tourfolio.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "collection_spots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "collection_id", nullable = false)
    private Long collectionId;

    @Column(name = "spot_id", nullable = false)
    private Long spotId;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;
}