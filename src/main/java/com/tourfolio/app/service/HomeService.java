package com.tourfolio.app.service;

import com.tourfolio.app.dto.HomeResponse;
import com.tourfolio.app.dto.PortfolioSummaryResponse;
import com.tourfolio.app.repository.CardRepository;
import com.tourfolio.app.repository.PortfolioRepository;
import com.tourfolio.app.repository.SpotRepository;
import com.tourfolio.app.repository.UserCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HomeService {

    private final StockService stockService;
    private final PortfolioRepository portfolioRepository;
    private final CardRepository cardRepository;
    private final UserCardRepository userCardRepository;
    private final SpotRepository spotRepository;

    private static final String DEFAULT_IMAGE_URL = "https://via.placeholder.com/800x600?text=No+Image";

    public HomeResponse getHome(Long userId) {
        // 1. 내 포트폴리오 요약 (기존 로직 재사용)
        PortfolioSummaryResponse summary = stockService.getPortfolioSummary(userId, "1W");
        int stockCount = portfolioRepository.findByMemberId(userId).size();

        HomeResponse.PortfolioSummary portfolio = HomeResponse.PortfolioSummary.builder()
                .totalAsset(summary.getTotalAsset())
                .todayProfit(summary.getTotalProfitLoss())
                .todayProfitRate(summary.getProfitRate())
                .stockCount(stockCount)
                .build();

        // 2. 보유 카드 요약
        long ownedCount = userCardRepository.countByUserId(userId);
        long totalCount = cardRepository.count();
        double collectionRate = totalCount > 0 ? Math.round((ownedCount * 1000.0 / totalCount)) / 10.0 : 0.0;

        HomeResponse.CardCollectionSummary cardCollection = HomeResponse.CardCollectionSummary.builder()
                .ownedCount(ownedCount)
                .totalCount(totalCount)
                .collectionRate(collectionRate)
                .build();

        // 3. 이번주 추천 관광지 (관리자가 DB에서 직접 지정)
        List<HomeResponse.RecommendedSpotItem> recommended = spotRepository.findByIsWeeklyRecommendedTrueOrderByRecommendOrderAsc().stream()
                .map(spot -> HomeResponse.RecommendedSpotItem.builder()
                        .spotId(spot.getId())
                        .name(spot.getName())
                        .imageUrl(spot.getImageUrl() != null && !spot.getImageUrl().isEmpty() ? spot.getImageUrl() : DEFAULT_IMAGE_URL)
                        .region(spot.getAreaName() != null ? spot.getAreaName() : spot.getRegion())
                        .tags(parseTags(spot.getThemeTag()))
                        .build())
                .collect(Collectors.toList());

        log.info("홈 화면 조회 완료: userId={}, stockCount={}, cardCount={}/{}, recommended={}건",
                userId, stockCount, ownedCount, totalCount, recommended.size());

        return HomeResponse.builder()
                .portfolio(portfolio)
                .cardCollection(cardCollection)
                .recommendedSpots(recommended)
                .build();
    }

    private List<String> parseTags(String themeTag) {
        if (themeTag == null || themeTag.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(themeTag.split(","))
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .collect(Collectors.toList());
    }
}