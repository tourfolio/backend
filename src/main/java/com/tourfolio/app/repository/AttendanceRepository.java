package com.tourfolio.app.repository;

import com.tourfolio.app.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByMemberIdAndAttendanceDateAfter(Long memberId, LocalDateTime attendanceDate);

    @Query("SELECT COUNT(a) > 0 FROM Attendance a WHERE a.memberId = :memberId AND a.attendanceDate >= :startDate")
    boolean existsByMemberIdAndAttendanceDateAfter(@Param("memberId") Long memberId, @Param("startDate") LocalDateTime startDate);

    List<Attendance> findByMemberIdOrderByAttendanceDateDesc(Long memberId);

    Long countByMemberId(Long memberId);
}
