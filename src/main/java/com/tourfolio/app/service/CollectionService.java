package com.tourfolio.app.service;

import com.tourfolio.app.dto.*;
import com.tourfolio.app.entity.Card;
import com.tourfolio.app.entity.Spot;
import com.tourfolio.app.entity.UserCard;
import com.tourfolio.app.exception.CustomException;
import com.tourfolio.app.repository.CardRepository;
import com.tourfolio.app.repository.SpotRepository;
import com.tourfolio.app.repository.UserCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CollectionService {

    private final CardRepository cardRepository;
    private final UserCardRepository userCardRepository;
    private final SpotRepository spotRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final double DISTANCE_THRESHOLD = 200.0; // 200m 반경

    // 수집 메인 화면 조회 (복합 필터링)
    public CollectionResponse getCollection(Long userId, String region, String theme, Card.CardRarity rarity) {
        log.info("수집 메인 화면 조회 시작: userId={}, region={}, theme={}, rarity={}", userId, region, theme, rarity);

        List<Card> cards = cardRepository.findCardsWithFilters(region, theme, rarity);
        List<Long> ownedCardIds = userCardRepository.findCardIdsByUserId(userId);

        List<CardSummary> cardSummaries = cards.stream()
                .map(card -> {
                    Spot spot = spotRepository.findById(card.getSpotId()).orElse(null);
                    return CardSummary.builder()
                            .cardId(card.getId())
                            .spotName(spot != null ? spot.getName() : "알 수 없음")
                            .imageUrl(spot != null ? spot.getImageUrl() : "")
                            .rarity(card.getRarity())
                            .theme(card.getTheme())
                            .region(spot != null ? spot.getRegion() : "")
                            .isOwned(ownedCardIds.contains(card.getId()))
                            .build();
                })
                .collect(Collectors.toList());

        int totalCount = cards.size();
        int ownedCount = (int) cardSummaries.stream().filter(CardSummary::getIsOwned).count();
        double collectionRate = totalCount > 0 ? (ownedCount * 100.0 / totalCount) : 0.0;

        log.info("수집 메인 화면 조회 완료: totalCount={}, ownedCount={}, collectionRate={}", totalCount, ownedCount, collectionRate);
        return CollectionResponse.builder()
                .collectionRate(Math.round(collectionRate * 10.0) / 10.0)
                .ownedCount(ownedCount)
                .totalCount(totalCount)
                .cards(cardSummaries)
                .build();
    }

    // 포토카드 상세 조회
    public CardDetailResponse getCardDetail(Long userId, Long cardId) {
        log.info("포토카드 상세 조회 시작: userId={}, cardId={}", userId, cardId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CustomException("CARD_NOT_FOUND", "카드를 찾을 수 없습니다."));

        Spot spot = spotRepository.findById(card.getSpotId())
                .orElseThrow(() -> new CustomException("SPOT_NOT_FOUND", "관광지를 찾을 수 없습니다."));

        UserCard userCard = userCardRepository.findByUserIdAndCardId(userId, cardId).orElse(null);
        boolean isOwned = userCard != null;

        CardDetailResponse.CardDetailResponseBuilder builder = CardDetailResponse.builder()
                .cardId(card.getId())
                .name(spot.getName() + " / " + spot.getName())
                .address(spot.getAddress() != null ? spot.getAddress() : spot.getRegion())
                .rarity(card.getRarity())
                .theme(card.getTheme())
                .imageUrl(spot.getImageUrl())
                .glowColorCode(card.getGlowColorCode())
                .cardNumber("No." + String.format("%02d", card.getId()))
                .phrase(card.getPhrase())
                .isOwned(isOwned);

        if (isOwned) {
            builder.acquiredAt(userCard.getAcquiredAt().format(DATE_FORMATTER))
                   .acquisitionPath(userCard.getAcquisitionPath());
        } else {
            builder.message("관광지를 직접 방문하여 카드를 획득하세요!");
        }

        log.info("포토카드 상세 조회 완료: cardId={}, isOwned={}", cardId, isOwned);
        return builder.build();
    }

    // GPS 기반 카드 획득 (방문 인증)
    @Transactional
    public void acquireCard(Long userId, Long cardId, AcquireCardRequest request) {
        log.info("GPS 기반 카드 획득 시작: userId={}, cardId={}, distanceInMeters={}",
                userId, cardId, request.getDistanceInMeters());

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CustomException("CARD_NOT_FOUND", "카드를 찾을 수 없습니다."));

        // 이미 보유 여부 확인
        if (userCardRepository.findByUserIdAndCardId(userId, cardId).isPresent()) {
            throw new CustomException("ALREADY_OWNED", "이미 보유한 카드입니다.");
        }

        // 거리 검증 (프론트엔드에서 계산한 거리값 사용)
        Double distanceInMeters = request.getDistanceInMeters();
        if (distanceInMeters == null) {
            throw new CustomException("INVALID_DISTANCE", "거리값이 제공되지 않았습니다.");
        }

        if (distanceInMeters > DISTANCE_THRESHOLD) {
            throw new CustomException("LOCATION_TOO_FAR",
                    String.format("아직 관광지와의 거리가 셉니다. 200m 이내로 접근해 주세요. (현재 거리: %.1fm)", distanceInMeters));
        }

        // 카드 획득
        UserCard userCard = UserCard.builder()
                .userId(userId)
                .cardId(cardId)
                .acquiredAt(java.time.LocalDateTime.now())
                .acquisitionPath("관광지 방문")
                .build();

        userCardRepository.save(userCard);
        log.info("GPS 기반 카드 획득 완료: userId={}, cardId={}, distanceInMeters={}", userId, cardId, distanceInMeters);
    }

    // 하버사인 공식으로 두 GPS 좌표 간의 거리 계산 (단위: 도)
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return c; // 라디안 단위 (약 111km = 1도)
    }
}
