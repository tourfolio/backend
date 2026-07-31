// src/main/java/com/tourfolio/app/repository/WatchlistRepository.java
package com.tourfolio.app.repository;

import com.tourfolio.app.entity.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    Optional<Watchlist> findByUserIdAndSpotId(Long userId, Long spotId);

    List<Watchlist> findByUserIdOrderByCreatedAtDesc(Long userId);

    void deleteByUserIdAndSpotId(Long userId, Long spotId);
}
