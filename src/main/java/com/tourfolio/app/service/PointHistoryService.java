package com.tourfolio.app.service;

import com.tourfolio.app.dto.PointHistoryResponse;
import com.tourfolio.app.entity.PointHistory;
import com.tourfolio.app.entity.User;
import com.tourfolio.app.exception.CustomException;
import com.tourfolio.app.repository.PointHistoryRepository;
import com.tourfolio.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PointHistoryService {

    private final PointHistoryRepository pointHistoryRepository;
    private final UserRepository userRepository;

    public PointHistoryResponse getHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));

        List<PointHistoryResponse.PointHistoryItem> items = pointHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(h -> PointHistoryResponse.PointHistoryItem.builder()
                        .title(h.getTitle())
                        .amount(h.getAmount())
                        .createdAt(h.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return PointHistoryResponse.builder()
                .balance(user.getBalance())
                .histories(items)
                .build();
    }
}