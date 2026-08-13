package com.tourfolio.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "공지사항 상세 DTO")
public class AnnouncementDetailResponse {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
}