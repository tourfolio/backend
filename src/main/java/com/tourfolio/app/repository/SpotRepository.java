package com.tourfolio.app.repository;

import com.tourfolio.app.entity.Spot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpotRepository extends JpaRepository<Spot, Long> {

    List<Spot> findAllByOrderByTierAscNameAsc();
}
