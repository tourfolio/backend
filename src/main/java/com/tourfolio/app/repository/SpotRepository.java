// src/main/java/com/tourfolio/app/repository/SpotRepository.java
package com.tourfolio.app.repository;

import com.tourfolio.app.entity.Spot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SpotRepository extends JpaRepository<Spot, Long> {

    List<Spot> findAllByOrderByTierAscNameAsc();

    @Query("SELECT s FROM Spot s WHERE (:region IS NULL OR s.region = :region) AND (:theme IS NULL OR s.theme = :theme)")
    List<Spot> findByRegionAndTheme(@Param("region") String region, @Param("theme") String theme);

    @Query("SELECT s FROM Spot s WHERE (:region IS NULL OR s.region = :region) AND (:theme IS NULL OR s.theme = :theme) ORDER BY s.currentPrice DESC")
    List<Spot> findByRegionAndThemeOrderByPriceDesc(@Param("region") String region, @Param("theme") String theme);

    @Query("SELECT s FROM Spot s WHERE (:region IS NULL OR s.region = :region) AND (:theme IS NULL OR s.theme = :theme) ORDER BY s.currentPrice ASC")
    List<Spot> findByRegionAndThemeOrderByPriceAsc(@Param("region") String region, @Param("theme") String theme);

    @Query("SELECT s FROM Spot s WHERE (:region IS NULL OR s.region = :region) AND (:theme IS NULL OR s.theme = :theme) ORDER BY (s.currentPrice - s.prevPrice) / s.prevPrice DESC")
    List<Spot> findByRegionAndThemeOrderByChangeRateDesc(@Param("region") String region, @Param("theme") String theme);

    @Query("SELECT s FROM Spot s WHERE (:region IS NULL OR s.region = :region) AND (:theme IS NULL OR s.theme = :theme) ORDER BY (s.currentPrice - s.prevPrice) / s.prevPrice ASC")
    List<Spot> findByRegionAndThemeOrderByChangeRateAsc(@Param("region") String region, @Param("theme") String theme);

    @Query("SELECT s FROM Spot s WHERE (:region IS NULL OR s.region = :region) AND (:theme IS NULL OR s.theme = :theme) ORDER BY s.tier ASC")
    List<Spot> findByRegionAndThemeOrderByTierAsc(@Param("region") String region, @Param("theme") String theme);

    @Query("SELECT s FROM Spot s ORDER BY (s.currentPrice - s.prevPrice) / s.prevPrice DESC")
    List<Spot> findAllOrderByChangeRateDesc();

    @Query("SELECT s FROM Spot s ORDER BY (s.currentPrice - s.prevPrice) / s.prevPrice ASC")
    List<Spot> findAllOrderByChangeRateAsc();

    @Query("SELECT s FROM Spot s WHERE s.region = :region")
    List<Spot> findByRegion(@Param("region") String region);

    @Query("SELECT s FROM Spot s WHERE s.theme = :theme")
    List<Spot> findByTheme(@Param("theme") String theme);

    @Query("SELECT s FROM Spot s WHERE (:keyword IS NULL OR :keyword = '' OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND (:areaCode IS NULL OR :areaCode = '' OR s.areaCode = :areaCode) AND (:themeTag IS NULL OR :themeTag = '' OR s.themeTag = :themeTag)")
    List<Spot> searchExploreCards(@Param("keyword") String keyword, @Param("areaCode") String areaCode, @Param("themeTag") String themeTag);
}