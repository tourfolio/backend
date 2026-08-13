package com.tourfolio.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "알림 목록 응답 DTO")
public class NotificationListResponse {

    private List<NotificationItem> newNotifications;
    private List<NotificationItem> pastNotifications;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationItem {
        private Long id;
        private String type;
        private String message;
        private LocalDateTime createdAt;
    }
}