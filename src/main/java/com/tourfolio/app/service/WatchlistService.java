// src/main/java/com/tourfolio/app/service/WatchlistService.java
package com.tourfolio.app.service;

import com.tourfolio.app.dto.WatchlistResponse;
import com.tourfolio.app.entity.Watchlist;
import com.tourfolio.app.entity.Spot;
import com.tourfolio.app.exception.CustomException;
import com.tourfolio.app.repository.WatchlistRepository;
import com.tourfolio.app.repository.SpotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final SpotRepository spotRepository;

    @Transactional(rollbackFor = Exception.class)
    public Watchlist addToWatchlist(Long memberId, Long spotId) {
        Spot spot = spotRepository.findById(spotId)
                .orElseThrow(() -> new CustomException("SPOT_NOT_FOUND", "상장되지 않은 관광 자산 종목입니다. ID: " + spotId));

        if (watchlistRepository.findByMemberIdAndSpotId(memberId, spotId).isPresent()) {
            throw new CustomException("ALREADY_IN_WATCHLIST", "이미 관심 목록에 추가된 종목입니다.");
        }

        Watchlist watchlist = Watchlist.builder()
                .memberId(memberId)
                .spotId(spotId)
                .createdAt(java.time.LocalDateTime.now())
                .build();

        log.info("관심 목록 추가: memberId={}, spotId={}", memberId, spotId);
        return watchlistRepository.save(watchlist);
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeFromWatchlist(Long memberId, Long spotId) {
        Watchlist watchlist = watchlistRepository.findByMemberIdAndSpotId(memberId, spotId)
                .orElseThrow(() -> new CustomException("NOT_IN_WATCHLIST", "관심 목록에 없는 종목입니다."));

        watchlistRepository.delete(watchlist);
        log.info("관심 목록 제거: memberId={}, spotId={}", memberId, spotId);
    }

    public List<WatchlistResponse> getWatchlist(Long memberId) {
        List<Watchlist> watchlists = watchlistRepository.findByMemberIdOrderByCreatedAtDesc(memberId);

        return watchlists.stream()
                .map(watchlist -> {
                    Spot spot = spotRepository.findById(watchlist.getSpotId()).orElse(null);
                    if (spot == null) {
                        log.warn("관광지 정보를 찾을 수 없음: spotId={}", watchlist.getSpotId());
                        return null;
                    }

                    BigDecimal changeRate = BigDecimal.ZERO;
                    if (spot.getPrevPrice().compareTo(BigDecimal.ZERO) > 0) {
                        changeRate = spot.getCurrentPrice().subtract(spot.getPrevPrice())
                                .divide(spot.getPrevPrice(), 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                .setScale(2, RoundingMode.HALF_UP);
                    }

                    return WatchlistResponse.builder()
                            .id(watchlist.getId())
                            .spotId(spot.getId())
                            .spotName(spot.getName())
                            .region(spot.getRegion())
                            .theme(spot.getTheme())
                            .currentPrice(spot.getCurrentPrice())
                            .changeRate(changeRate)
                            .createdAt(watchlist.getCreatedAt())
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
