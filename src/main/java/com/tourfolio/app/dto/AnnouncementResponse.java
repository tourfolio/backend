package com.tourfolio.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "공지사항 목록 아이템 DTO")
public class AnnouncementResponse {
    private Long id;
    private String title;
    private LocalDateTime createdAt;
}