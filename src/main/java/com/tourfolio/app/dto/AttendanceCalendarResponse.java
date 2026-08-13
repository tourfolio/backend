package com.tourfolio.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "월간 출석 캘린더 DTO")
public class AttendanceCalendarResponse {

    private Integer year;
    private Integer month;
    private Integer attendedCount; // 이번달 출석 횟수
    private List<String> attendedDates; // "yyyy-MM-dd" 목록
}