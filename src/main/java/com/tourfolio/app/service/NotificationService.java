package com.tourfolio.app.service;

import com.tourfolio.app.dto.NotificationListResponse;
import com.tourfolio.app.entity.Notification;
import com.tourfolio.app.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // 다른 서비스(출석/거래/카드획득/미션)에서 알림 생성할 때 호출
    public void notify(Long userId, String type, String message) {
        notificationRepository.save(Notification.builder()
                .userId(userId)
                .type(type)
                .message(message)
                .isRead(false)
                .build());
        log.info("알림 생성: userId={}, type={}, message={}", userId, type, message);
    }

    // 조회 시점에 미읽음 알림을 전부 읽음 처리 (다음 조회부턴 "지난 알림"으로)
    @Transactional
    public NotificationListResponse getNotifications(Long userId) {
        List<Notification> all = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);

        List<NotificationListResponse.NotificationItem> newList = new ArrayList<>();
        List<NotificationListResponse.NotificationItem> pastList = new ArrayList<>();

        for (Notification n : all) {
            NotificationListResponse.NotificationItem item = NotificationListResponse.NotificationItem.builder()
                    .id(n.getId())
                    .type(n.getType())
                    .message(n.getMessage())
                    .createdAt(n.getCreatedAt())
                    .build();

            if (Boolean.TRUE.equals(n.getIsRead())) {
                pastList.add(item);
            } else {
                newList.add(item);
                n.setIsRead(true);
            }
        }
        notificationRepository.saveAll(all);

        return NotificationListResponse.builder()
                .newNotifications(newList)
                .pastNotifications(pastList)
                .build();
    }
}