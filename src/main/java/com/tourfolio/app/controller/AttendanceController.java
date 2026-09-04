package com.tourfolio.app.controller;

import com.tourfolio.app.dto.AttendanceCalendarResponse;
import com.tourfolio.app.dto.AttendanceCheckResponse;
import com.tourfolio.app.security.SecurityUtil;
import com.tourfolio.app.service.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@Tag(name = "출석체크", description = "출석체크 및 출석 캘린더 조회 API")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/check")
    @Operation(summary = "출석체크 하기", description = "오늘 출석체크를 하고 포인트(500P)를 지급받습니다.(토큰 필요)")
    public ResponseEntity<AttendanceCheckResponse> checkIn() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("POST /api/v1/attendance/check - 출석체크 요청: userId={}", userId);
        return ResponseEntity.ok(attendanceService.checkIn(userId));
    }

    @GetMapping("/calendar")
    @Operation(summary = "월간 출석 캘린더 조회", description = "(토큰 필요)")
    public ResponseEntity<AttendanceCalendarResponse> getCalendar(
            @Parameter(description = "연도", example = "2026") @RequestParam int year,
            @Parameter(description = "월", example = "8") @RequestParam int month) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("GET /api/v1/attendance/calendar - 캘린더 조회: userId={}, year={}, month={}", userId, year, month);
        return ResponseEntity.ok(attendanceService.getCalendar(userId, year, month));
    }
}