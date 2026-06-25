package com.tourfolio.app.repository;

import com.tourfolio.app.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    List<Portfolio> findByMemberId(Long memberId);
    Optional<Portfolio> findByMemberIdAndSpotId(Long memberId, Long spotId);
}