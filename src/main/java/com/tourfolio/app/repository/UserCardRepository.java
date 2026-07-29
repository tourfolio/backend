package com.tourfolio.app.repository;

import com.tourfolio.app.entity.UserCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCardRepository extends JpaRepository<UserCard, Long> {

    Optional<UserCard> findByUserIdAndCardId(Long userId, Long cardId);

    List<UserCard> findByUserId(Long userId);

    @Query("SELECT uc.cardId FROM UserCard uc WHERE uc.userId = :userId")
    List<Long> findCardIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(uc) FROM UserCard uc WHERE uc.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);
}
