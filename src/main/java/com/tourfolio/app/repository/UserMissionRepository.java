package com.tourfolio.app.repository;

import com.tourfolio.app.entity.UserMission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMissionRepository extends JpaRepository<UserMission, Long> {
    List<UserMission> findByUserId(Long userId);
    Optional<UserMission> findByUserIdAndMissionId(Long userId, Long missionId);

    @Modifying
    @Query("UPDATE UserMission um SET um.isCompleted = true, um.completedAt = CURRENT_TIMESTAMP, um.currentProgress = :target " +
            "WHERE um.userId = :userId AND um.missionId = :missionId AND (um.isCompleted = false OR um.isCompleted IS NULL)")
    int markCompletedIfNotAlready(@Param("userId") Long userId,
                                  @Param("missionId") Long missionId,
                                  @Param("target") Integer target);
}