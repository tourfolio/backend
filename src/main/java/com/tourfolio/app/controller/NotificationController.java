package com.tourfolio.app.controller;

import com.tourfolio.app.dto.NotificationListResponse;
import com.tourfolio.app.security.SecurityUtil;
import com.tourfolio.app.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "알림", description = "알림 목록 조회 API")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "알림 목록 조회", description = "새로운 알림과 지난 알림을 나눠서 반환합니다. 조회 시 미읽음 알림은 읽음 처리됩니다.")
    public ResponseEntity<NotificationListResponse> getNotifications() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("GET /api/v1/notifications - 알림 목록 조회: userId={}", userId);
        return ResponseEntity.ok(notificationService.getNotifications(userId));
    }

    @PostMapping("/location-permission")
    @Operation(summary = "위치 권한 허용 알림 생성", description = "프론트에서 위치 권한이 허용된 시점에 호출합니다. 별도 요청값 없이 알림만 생성합니다.")
    public ResponseEntity<Void> createLocationPermissionNotification() {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("POST /api/v1/notifications/location-permission - 위치 권한 알림 생성: userId={}", userId);
        notificationService.notify(userId, "LOCATION_PERMISSION", "위치 권한 설정을 허용하였습니다.");
        return ResponseEntity.ok().build();
    }
}