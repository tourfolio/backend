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
    private final NotificationService notificationService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    // 수집 메인 화면 조회 (복합 필터링) — 요약은 항상 전체 카드 기준, 목록만 필터링됨
    public CollectionResponse getCollection(Long userId, String region, String theme, Card.CardRarity rarity) {
        log.info("수집 메인 화면 조회 시작: userId={}, region={}, theme={}, rarity={}", userId, region, theme, rarity);

        List<Long> ownedCardIds = userCardRepository.findCardIdsByUserId(userId);

        // 1. 전체 수집 요약 (필터와 무관하게 항상 전체 카드 기준으로 계산)
        List<Card> allCards = cardRepository.findAll();
        int totalCount = allCards.size();
        int ownedCount = (int) allCards.stream()
                .filter(card -> ownedCardIds.contains(card.getId()))
                .count();
        double collectionRate = totalCount > 0 ? Math.round((ownedCount * 1000.0 / totalCount)) / 10.0 : 0.0;

        CollectionResponse.CollectionSummary summary = CollectionResponse.CollectionSummary.builder()
                .collectionRate(collectionRate)
                .ownedCount(ownedCount)
                .totalCount(totalCount)
                .build();

        // 2. 필터링된 카드 목록
        List<Card> filteredCards = cardRepository.findCardsWithFilters(region, theme, rarity);
        List<CardSummary> cardSummaries = filteredCards.stream()
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

        log.info("수집 메인 화면 조회 완료: totalCount={}, ownedCount={}, collectionRate={}, filteredCount={}",
                totalCount, ownedCount, collectionRate, cardSummaries.size());

        return CollectionResponse.builder()
                .summary(summary)
                .filteredCount(cardSummaries.size())
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
    // 카드 획득 시도 전, 관광지 좌표만 가볍게 조회 (앱에서 거리 계산용)
    public CardLocationResponse getCardLocation(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CustomException("CARD_NOT_FOUND", "카드를 찾을 수 없습니다."));

        Spot spot = spotRepository.findById(card.getSpotId())
                .orElseThrow(() -> new CustomException("SPOT_NOT_FOUND", "관광지를 찾을 수 없습니다."));

        return CardLocationResponse.builder()
                .cardId(card.getId())
                .spotName(spot.getName())
                .latitude(card.getLatitude())
                .longitude(card.getLongitude())
                .build();
    }

    // 카드 획득 (방문 인증) — 위치 정보는 앱에서만 확인, 서버는 검증 없이 지급
    @Transactional
    public AcquireCardResponse acquireCard(Long userId, Long cardId) {
        log.info("카드 획득 시작: userId={}, cardId={}", userId, cardId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CustomException("CARD_NOT_FOUND", "카드를 찾을 수 없습니다."));

        // 이미 보유 여부 확인
        if (userCardRepository.findByUserIdAndCardId(userId, cardId).isPresent()) {
            throw new CustomException("ALREADY_OWNED", "이미 보유한 카드입니다.");
        }

        Spot spot = spotRepository.findById(card.getSpotId())
                .orElseThrow(() -> new CustomException("SPOT_NOT_FOUND", "관광지를 찾을 수 없습니다."));

        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        // 카드 획득
        UserCard userCard = UserCard.builder()
                .userId(userId)
                .cardId(cardId)
                .acquiredAt(now)
                .acquisitionPath("관광지 방문")
                .build();

        userCardRepository.save(userCard);

        notificationService.notify(userId, "CARD_ACQUIRED", spot.getName() + " 카드를 획득했습니다.");

        log.info("카드 획득 완료: userId={}, cardId={}", userId, cardId);

        return AcquireCardResponse.builder()
                .cardId(card.getId())
                .cardName(spot.getName())
                .rarity(card.getRarity())
                .acquiredAt(now.format(DATE_FORMATTER))
                .build();
    }

}