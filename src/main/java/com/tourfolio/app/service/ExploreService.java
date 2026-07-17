// src/main/java/com/tourfolio/app/service/ExploreService.java
package com.tourfolio.app.service;

import com.tourfolio.app.dto.ExploreResponse;
import com.tourfolio.app.entity.Spot;
import com.tourfolio.app.repository.SpotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExploreService {

    private final SpotRepository spotRepository;

    public List<ExploreResponse> getAllExploreCards() {
        log.info("탐색 화면 전체 카드 조회 시작");
        List<Spot> spots = spotRepository.findAllByOrderByTierAscNameAsc();
        List<ExploreResponse> responses = spots.stream()
                .map(this::mapToExploreResponse)
                .collect(Collectors.toList());
        log.info("탐색 화면 전체 카드 조회 완료: {}건", responses.size());
        return responses;
    }

    public List<ExploreResponse> searchExploreCards(String keyword, String areaCode, String themeTag) {
        log.info("탐색 화면 복합 필터링 조회 시작: keyword={}, areaCode={}, themeTag={}", keyword, areaCode, themeTag);
        
        String normalizedKeyword = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
        String normalizedAreaCode = (areaCode == null || areaCode.trim().isEmpty()) ? null : areaCode.trim();
        String normalizedThemeTag = (themeTag == null || themeTag.trim().isEmpty()) ? null : themeTag.trim();
        
        List<Spot> spots = spotRepository.searchExploreCards(normalizedKeyword, normalizedAreaCode, normalizedThemeTag);
        List<ExploreResponse> responses = spots.stream()
                .map(this::mapToExploreResponse)
                .collect(Collectors.toList());
        log.info("탐색 화면 복합 필터링 조회 완료: {}건", responses.size());
        return responses;
    }

    private ExploreResponse mapToExploreResponse(Spot spot) {
        BigDecimal changeRate = BigDecimal.ZERO;
        if (spot.getPrevPrice().compareTo(BigDecimal.ZERO) > 0) {
            changeRate = spot.getCurrentPrice().subtract(spot.getPrevPrice())
                    .divide(spot.getPrevPrice(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return ExploreResponse.builder()
                .id(spot.getId())
                .name(spot.getName())
                .areaCode(spot.getAreaCode())
                .areaName(spot.getAreaName() != null ? spot.getAreaName() : spot.getRegion())
                .themeTag(spot.getThemeTag() != null ? spot.getThemeTag() : spot.getTheme())
                .tier(spot.getTier())
                .imageUrl(spot.getImageUrl())
                .description(spot.getDescription())
                .currentPrice(spot.getCurrentPrice())
                .changeRate(changeRate)
                .tourismScore(spot.getTourismDataWeight())
                .build();
    }
}
