// src/main/java/com/tourfolio/app/service/WatchlistService.java
package com.tourfolio.app.service;

import com.tourfolio.app.dto.WatchlistResponse;
import com.tourfolio.app.entity.Watchlist;
import com.tourfolio.app.entity.Spot;
import com.tourfolio.app.entity.Member;
import com.tourfolio.app.exception.CustomException;
import com.tourfolio.app.repository.WatchlistRepository;
import com.tourfolio.app.repository.SpotRepository;
import com.tourfolio.app.repository.MemberRepository;
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
    private final MemberRepository memberRepository;

    @Transactional(rollbackFor = Exception.class)
    public Watchlist addToWatchlist(Long memberId, Long spotId) {
        try {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new CustomException("MEMBER_NOT_FOUND", "존재하지 않는 회원입니다. ID: " + memberId));

            Spot spot = spotRepository.findById(spotId)
                    .orElseThrow(() -> new CustomException("SPOT_NOT_FOUND", "상장되지 않은 관광 자산 종목입니다. ID: " + spotId));

            if (watchlistRepository.findByMemberIdAndSpotId(memberId, spotId).isPresent()) {
                throw new CustomException("ALREADY_IN_WATCHLIST", "이미 관심 목록에 추가된 종목입니다.");
            }

            Watchlist watchlist = Watchlist.builder()
                    .memberId(memberId)
                    .spotId(spotId)
                    .member(null)
                    .spot(null)
                    .build();

            log.info("관심 목록 추가: memberId={}, spotId={}", memberId, spotId);
            return watchlistRepository.save(watchlist);
        } catch (CustomException e) {
            log.error("관심 목록 추가 실패 (비즈니스 예외): memberId={}, spotId={}, error={}", memberId, spotId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("관심 목록 추가 실패 (시스템 예외): memberId={}, spotId={}", memberId, spotId, e);
            throw new CustomException("WATCHLIST_ADD_FAILED", "관심 목록 추가 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeFromWatchlist(Long memberId, Long spotId) {
        try {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new CustomException("MEMBER_NOT_FOUND", "존재하지 않는 회원입니다. ID: " + memberId));

            Watchlist watchlist = watchlistRepository.findByMemberIdAndSpotId(memberId, spotId)
                    .orElseThrow(() -> new CustomException("NOT_IN_WATCHLIST", "관심 목록에 없는 종목입니다."));

            watchlistRepository.delete(watchlist);
            log.info("관심 목록 제거: memberId={}, spotId={}", memberId, spotId);
        } catch (CustomException e) {
            log.error("관심 목록 제거 실패 (비즈니스 예외): memberId={}, spotId={}, error={}", memberId, spotId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("관심 목록 제거 실패 (시스템 예외): memberId={}, spotId={}", memberId, spotId, e);
            throw new CustomException("WATCHLIST_REMOVE_FAILED", "관심 목록 제거 중 오류가 발생했습니다.");
        }
    }

    public List<WatchlistResponse> getWatchlist(Long memberId) {
        try {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new CustomException("MEMBER_NOT_FOUND", "존재하지 않는 회원입니다. ID: " + memberId));

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
        } catch (CustomException e) {
            log.error("관심 목록 조회 실패 (비즈니스 예외): memberId={}, error={}", memberId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("관심 목록 조회 실패 (시스템 예외): memberId={}", memberId, e);
            throw new CustomException("WATCHLIST_GET_FAILED", "관심 목록 조회 중 오류가 발생했습니다.");
        }
    }
}
