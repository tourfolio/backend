package com.tourfolio.app.repository;

import com.tourfolio.app.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findBySpotIdAndCreatedAtAfterOrderByCreatedAtAsc(Long spotId, LocalDateTime dateTime);

    List<Transaction> findBySpotIdAndCreatedAtBetweenOrderByCreatedAtAsc(Long spotId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT SUM(CASE WHEN t.type = 'BUY' THEN t.quantity ELSE -t.quantity END) " +
            "FROM Transaction t WHERE t.spotId = :spotId AND t.createdAt >= :startTime")
    BigDecimal calculateNetBuyVolume(@Param("spotId") Long spotId, @Param("startTime") LocalDateTime startTime);

    List<Transaction> findBySpotIdOrderByCreatedAtDesc(Long spotId);
    List<Transaction> findByMemberIdOrderByExecutedAtDesc(Long memberId);
}