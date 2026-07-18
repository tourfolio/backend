// src/main/java/com/tourfolio/app/repository/PriceHistoryRepository.java
package com.tourfolio.app.repository;

import com.tourfolio.app.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    PriceHistory findBySpotIdAndTradeDate(Long spotId, LocalDate tradeDate);

    List<PriceHistory> findBySpotIdOrderByTradeDateAsc(Long spotId);

    @Query("SELECT ph FROM PriceHistory ph WHERE ph.spotId = :spotId AND ph.tradeDate >= :startDate ORDER BY ph.tradeDate ASC")
    List<PriceHistory> findBySpotIdAndTradeDateAfterOrderByTradeDateAsc(@Param("spotId") Long spotId, @Param("startDate") LocalDate startDate);

    @Query("SELECT ph FROM PriceHistory ph WHERE ph.spotId = :spotId AND ph.tradeDate BETWEEN :startDate AND :endDate ORDER BY ph.tradeDate ASC")
    List<PriceHistory> findBySpotIdAndTradeDateBetweenOrderByTradeDateAsc(@Param("spotId") Long spotId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT ph FROM PriceHistory ph WHERE ph.spotId = :spotId ORDER BY ph.tradeDate DESC LIMIT 1")
    PriceHistory findFirstBySpotIdOrderByTradeDateDesc(@Param("spotId") Long spotId);
}
