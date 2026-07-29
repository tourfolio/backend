package com.tourfolio.app.repository;

import com.tourfolio.app.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    @Query("SELECT c FROM Card c WHERE (:region IS NULL OR c.spotId IN (SELECT s.id FROM Spot s WHERE s.region = :region)) AND (:theme IS NULL OR c.theme = :theme) AND (:rarity IS NULL OR c.rarity = :rarity)")
    List<Card> findCardsWithFilters(@Param("region") String region, @Param("theme") String theme, @Param("rarity") Card.CardRarity rarity);

    Optional<Card> findBySpotId(Long spotId);

    @Query("SELECT s.id FROM Spot s WHERE s.id NOT IN (SELECT c.spotId FROM Card c)")
    List<Long> findSpotIdsWithoutCards();
}
