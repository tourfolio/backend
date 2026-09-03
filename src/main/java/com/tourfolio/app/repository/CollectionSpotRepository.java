package com.tourfolio.app.repository;

import com.tourfolio.app.entity.CollectionSpot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectionSpotRepository extends JpaRepository<CollectionSpot, Long> {
    List<CollectionSpot> findByCollectionIdOrderByDisplayOrderAsc(Long collectionId);
    int countByCollectionId(Long collectionId);
}