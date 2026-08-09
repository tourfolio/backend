// src/main/java/com/tourfolio/app/repository/StockSpotRepository.java
package com.tourfolio.app.repository;

import com.tourfolio.app.entity.StockSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockSpotRepository extends JpaRepository<StockSpot, Long> {

    List<StockSpot> findAllByOrderByTierAscNameAsc();

    @Query("SELECT s FROM StockSpot s WHERE (:region IS NULL OR s.regionName = :region) AND (:theme IS NULL OR s.theme = :theme)")
    List<StockSpot> findByRegionAndTheme(@Param("region") String region, @Param("theme") String theme);

    @Query("SELECT s FROM StockSpot s WHERE (:region IS NULL OR s.regionName = :region) AND (:theme IS NULL OR s.theme = :theme) ORDER BY s.currentPrice DESC")
    List<StockSpot> findByRegionAndThemeOrderByPriceDesc(@Param("region") String region, @Param("theme") String theme);

    @Query("SELECT s FROM StockSpot s WHERE (:region IS NULL OR s.regionName = :region) AND (:theme IS NULL OR s.theme = :theme) ORDER BY s.currentPrice ASC")
    List<StockSpot> findByRegionAndThemeOrderByPriceAsc(@Param("region") String region, @Param("theme") String theme);

    @Query("SELECT s FROM StockSpot s WHERE (:region IS NULL OR s.regionName = :region) AND (:theme IS NULL OR s.theme = :theme) ORDER BY s.changeRate DESC")
    List<StockSpot> findByRegionAndThemeOrderByChangeRateDesc(@Param("region") String region, @Param("theme") String theme);

    @Query("SELECT s FROM StockSpot s WHERE (:region IS NULL OR s.regionName = :region) AND (:theme IS NULL OR s.theme = :theme) ORDER BY s.changeRate ASC")
    List<StockSpot> findByRegionAndThemeOrderByChangeRateAsc(@Param("region") String region, @Param("theme") String theme);

    @Query("SELECT s FROM StockSpot s WHERE (:region IS NULL OR s.regionName = :region) AND (:theme IS NULL OR s.theme = :theme) ORDER BY s.tier ASC")
    List<StockSpot> findByRegionAndThemeOrderByTierAsc(@Param("region") String region, @Param("theme") String theme);

    @Query("SELECT s FROM StockSpot s ORDER BY s.changeRate DESC")
    List<StockSpot> findAllOrderByChangeRateDesc();

    @Query("SELECT s FROM StockSpot s ORDER BY s.changeRate ASC")
    List<StockSpot> findAllOrderByChangeRateAsc();

    @Query("SELECT s FROM StockSpot s WHERE s.regionName = :region")
    List<StockSpot> findByRegion(@Param("region") String region);

    @Query("SELECT s FROM StockSpot s WHERE s.theme = :theme")
    List<StockSpot> findByTheme(@Param("theme") String theme);
}
