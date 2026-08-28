// src/main/java/com/tourfolio/app/repository/TransactionRepository.java
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

    // [중요] 서비스에서 호출하는 바로 그 메서드입니다.
    List<Transaction> findBySpotIdAndCreatedAtBetweenOrderByCreatedAtAsc(Long spotId, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT SUM(CASE WHEN t.type = 'BUY' THEN t.quantity ELSE -t.quantity END) " +
            "FROM Transaction t WHERE t.spotId = :spotId AND t.createdAt >= :startTime")
    BigDecimal calculateNetBuyVolume(@Param("spotId") Long spotId, @Param("startTime") LocalDateTime startTime);

    List<Transaction> findBySpotIdOrderByCreatedAtDesc(Long spotId);
    List<Transaction> findByMemberIdOrderByExecutedAtDesc(Long memberId);

    @Query("SELECT COALESCE(SUM(t.quantity), 0) FROM Transaction t " +
            "WHERE t.spotId = :spotId AND t.createdAt BETWEEN :startTime AND :endTime")
    BigDecimal sumQuantityBySpotIdAndCreatedAtBetween(@Param("spotId") Long spotId,
                                                      @Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COALESCE(SUM(t.realizedProfit), 0) FROM Transaction t " +
            "WHERE t.memberId = :memberId AND t.type = 'SELL' AND t.createdAt BETWEEN :start AND :end")
    BigDecimal sumRealizedProfitByMemberIdAndCreatedAtBetween(@Param("memberId") Long memberId,
                                                              @Param("start") LocalDateTime start,
                                                              @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(t.totalAmount), 0) FROM Transaction t " +
            "WHERE t.memberId = :memberId AND t.type = 'SELL' AND t.createdAt BETWEEN :start AND :end")
    BigDecimal sumSellAmountByMemberIdAndCreatedAtBetween(@Param("memberId") Long memberId,
                                                          @Param("start") LocalDateTime start,
                                                          @Param("end") LocalDateTime end);
}