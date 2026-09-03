// src/main/java/com/tourfolio/app/service/AttendanceService.java
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
    private final NotificationService notificationService;

    private static final int DAILY_ATTENDANCE_POINTS = 1000;

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

        notificationService.notify(userId, "ATTENDANCE_POINT",
                "출석 체크 이벤트로 " + DAILY_ATTENDANCE_POINTS + "P 지급되었습니다");

        int consecutiveDays = calculateConsecutiveDays(userId);

        // 7일 단위 연속출석 마일스톤 알림 (7, 14, 21, 28일 ...)
        if (consecutiveDays > 0 && consecutiveDays % 7 == 0) {
            notificationService.notify(userId, "ATTENDANCE_STREAK",
                    consecutiveDays + "일 연속 출석체크했습니다");
        }

        log.info("출석체크 완료: userId={}, +{}P, 연속={}일", userId, DAILY_ATTENDANCE_POINTS, consecutiveDays);

        return AttendanceCheckResponse.builder()
                .pointsAwarded(DAILY_ATTENDANCE_POINTS)
                .balance(user.getBalance())
                .consecutiveDays(consecutiveDays)
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

    // 이번주(월~일) 출석 상태 배열 - MissionService에서도 사용
    // 값: ATTENDED(출석함) / MISSED(못함, 지난 날) / FUTURE(아직 안 온 날) / BEFORE_SIGNUP(가입 전 날짜)
    public List<String> calculateWeeklyAttendance(Long userId) {
        LocalDate monday = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
        LocalDate today = LocalDate.now();

        User user = userRepository.findById(userId).orElse(null);
        LocalDate signupDate = user != null ? user.getCreatedAt().toLocalDate() : null;

        var attendedDates = attendanceRepository.findByMemberIdOrderByAttendanceDateDesc(userId).stream()
                .map(a -> a.getAttendanceDate().toLocalDate())
                .collect(Collectors.toSet());

        return java.util.stream.IntStream.range(0, 7)
                .mapToObj(i -> {
                    LocalDate day = monday.plusDays(i);
                    if (attendedDates.contains(day)) {
                        return "ATTENDED";
                    } else if (day.isAfter(today)) {
                        return "FUTURE";
                    } else if (signupDate != null && day.isBefore(signupDate)) {
                        return "BEFORE_SIGNUP";
                    } else {
                        return "MISSED";
                    }
                })
                .collect(Collectors.toList());
    }

    // MissionService의 누적출석 미션 계산용
    public int getTotalAttendanceCount(Long userId) {
        return attendanceRepository.findByMemberIdOrderByAttendanceDateDesc(userId).size();
    }
}