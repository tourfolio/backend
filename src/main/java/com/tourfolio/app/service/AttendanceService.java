package com.tourfolio.app.service;

import com.tourfolio.app.dto.AttendanceCalendarResponse;
import com.tourfolio.app.dto.AttendanceCheckResponse;
import com.tourfolio.app.entity.Attendance;
import com.tourfolio.app.entity.PointHistory;
import com.tourfolio.app.entity.User;
import com.tourfolio.app.exception.CustomException;
import com.tourfolio.app.repository.AttendanceRepository;
import com.tourfolio.app.repository.PointHistoryRepository;
import com.tourfolio.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final PointHistoryRepository pointHistoryRepository;

    private static final int DAILY_ATTENDANCE_POINTS = 500;

    @Transactional
    public AttendanceCheckResponse checkIn(Long userId) {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

        if (attendanceRepository.existsByMemberIdAndAttendanceDateAfter(userId, startOfToday)) {
            throw new CustomException("ALREADY_CHECKED_IN", "오늘 이미 출석체크를 하셨습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));

        attendanceRepository.save(Attendance.builder()
                .memberId(userId)
                .attendanceDate(LocalDateTime.now())
                .pointsAwarded(DAILY_ATTENDANCE_POINTS)
                .build());

        user.setBalance(user.getBalance().add(BigDecimal.valueOf(DAILY_ATTENDANCE_POINTS)));
        userRepository.save(user);

        pointHistoryRepository.save(PointHistory.builder()
                .userId(userId)
                .type("ATTENDANCE")
                .title("출석 체크")
                .amount((long) DAILY_ATTENDANCE_POINTS)
                .createdAt(LocalDateTime.now())
                .build());

        log.info("출석체크 완료: userId={}, +{}P", userId, DAILY_ATTENDANCE_POINTS);

        return AttendanceCheckResponse.builder()
                .pointsAwarded(DAILY_ATTENDANCE_POINTS)
                .balance(user.getBalance())
                .consecutiveDays(calculateConsecutiveDays(userId))
                .build();
    }

    public AttendanceCalendarResponse getCalendar(Long userId, int year, int month) {
        List<String> dates = attendanceRepository.findByMemberIdOrderByAttendanceDateDesc(userId).stream()
                .map(a -> a.getAttendanceDate().toLocalDate())
                .filter(d -> d.getYear() == year && d.getMonthValue() == month)
                .distinct()
                .sorted()
                .map(LocalDate::toString)
                .collect(Collectors.toList());

        return AttendanceCalendarResponse.builder()
                .year(year)
                .month(month)
                .attendedCount(dates.size())
                .attendedDates(dates)
                .build();
    }

    // 미션 진행률 계산(MissionService)에서도 재사용
    public int calculateConsecutiveDays(Long userId) {
        List<LocalDate> dates = attendanceRepository.findByMemberIdOrderByAttendanceDateDesc(userId).stream()
                .map(a -> a.getAttendanceDate().toLocalDate())
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        if (dates.isEmpty()) return 0;

        LocalDate today = LocalDate.now();
        LocalDate mostRecent = dates.get(0);
        // 오늘이나 어제 출석 기록이 없으면 연속출석이 끊긴 것
        if (!mostRecent.equals(today) && !mostRecent.equals(today.minusDays(1))) {
            return 0;
        }

        int streak = 1;
        LocalDate cursor = mostRecent;
        for (int i = 1; i < dates.size(); i++) {
            LocalDate expected = cursor.minusDays(1);
            if (dates.get(i).equals(expected)) {
                streak++;
                cursor = expected;
            } else {
                break;
            }
        }
        return streak;
    }

    // 이번주(월~일) 출석 여부 배열 - MissionService에서도 사용
    public List<Boolean> calculateWeeklyAttendance(Long userId) {
        LocalDate monday = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        var attendedDates = attendanceRepository.findByMemberIdOrderByAttendanceDateDesc(userId).stream()
                .map(a -> a.getAttendanceDate().toLocalDate())
                .collect(Collectors.toSet());

        return java.util.stream.IntStream.range(0, 7)
                .mapToObj(i -> attendedDates.contains(monday.plusDays(i)))
                .collect(Collectors.toList());
    }

    // MissionService의 누적출석 미션 계산용
    public int getTotalAttendanceCount(Long userId) {
        return attendanceRepository.findByMemberIdOrderByAttendanceDateDesc(userId).size();
    }
}