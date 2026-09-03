package com.tourfolio.app.repository;

import com.tourfolio.app.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectionRepository extends JpaRepository<Collection, Long> {
    List<Collection> findAllByOrderByDisplayOrderAsc();
}