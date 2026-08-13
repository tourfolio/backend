package com.tourfolio.app.repository;

import com.tourfolio.app.entity.UserMission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMissionRepository extends JpaRepository<UserMission, Long> {
    List<UserMission> findByUserId(Long userId);
    Optional<UserMission> findByUserIdAndMissionId(Long userId, Long missionId);
}