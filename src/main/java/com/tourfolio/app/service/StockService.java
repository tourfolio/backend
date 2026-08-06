// src/main/java/com/tourfolio/app/service/StockService.java
package com.tourfolio.app.service;

import com.tourfolio.app.api.client.KorService2Client;
import com.tourfolio.app.api.dto.KorService2Dto;
import com.tourfolio.app.dto.StockResponse;
import com.tourfolio.app.dto.TradeRequest;
import com.tourfolio.app.dto.MemberAssetResponse;
import com.tourfolio.app.dto.PriceHistoryResponse;
import com.tourfolio.app.dto.RegionalIndexResponse;
import com.tourfolio.app.dto.StockChartResponse;
import com.tourfolio.app.dto.PortfolioSummaryResponse;
import com.tourfolio.app.entity.Spot;
import com.tourfolio.app.entity.Transaction;
import com.tourfolio.app.entity.User;
import com.tourfolio.app.entity.Portfolio;
import com.tourfolio.app.entity.PriceHistory;
import com.tourfolio.app.exception.CustomException;
import com.tourfolio.app.repository.SpotRepository;
import com.tourfolio.app.repository.TransactionRepository;
import com.tourfolio.app.repository.UserRepository;
import com.tourfolio.app.repository.PortfolioRepository;
import com.tourfolio.app.repository.PriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final SpotRepository spotRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final TourIndicatorService tourIndicatorService;
    private final PriceCalculationService priceCalculationService;
    private final KorService2Client korService2Client;

    @Transactional(rollbackFor = Exception.class)
    public Transaction executeTrade(TradeRequest request) {
        User user = userRepository.findById(request.getMemberId())
                .orElseThrow(() -> new CustomException("MEMBER_NOT_FOUND", "해당 사용자를 조회할 수 없습니다. ID: " + request.getMemberId()));

        Spot spot = spotRepository.findById(request.getSpotId())
                .orElseThrow(() -> new CustomException("SPOT_NOT_FOUND", "상장되지 않은 관광 자산 종목입니다. ID: " + request.getSpotId()));

        String type = request.getType().toUpperCase();
        if (!"BUY".equals(type) && !"SELL".equals(type)) {
            throw new CustomException("INVALID_TRADE_TYPE", "거래 타입은 오직 BUY 혹은 SELL만 허용됩니다.");
        }

        if (request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("INVALID_QUANTITY", "거래 수량은 0보다 커야 합니다.");
        }
        BigDecimal totalAmount = spot.getCurrentPrice().multiply(request.getQuantity()).setScale(2, RoundingMode.HALF_UP);

        if ("BUY".equals(type)) {
            if (user.getBalance().compareTo(totalAmount) < 0) {
                throw new CustomException("INSUFFICIENT_BALANCE", "보유 포인트 잔액이 부족하여 가상 매수가 불가능합니다.");
            }
            user.setBalance(user.getBalance().subtract(totalAmount));
            userRepository.save(user);

            // 포트폴리오 원장 갱신 및 평균 매수 평단가 가중치 계산
            Portfolio portfolio = portfolioRepository.findByMemberIdAndSpotId(user.getId(), spot.getId())
                    .orElse(Portfolio.builder()
                            .memberId(user.getId())
                            .spotId(spot.getId())
                            .quantity(BigDecimal.ZERO)
                            .averagePurchasePrice(BigDecimal.ZERO)
                            .updatedAt(LocalDateTime.now())
                            .build());

            BigDecimal oldTotalCost = portfolio.getQuantity().multiply(portfolio.getAveragePurchasePrice());
            BigDecimal newTotalCost = oldTotalCost.add(totalAmount);
            BigDecimal newQuantity = portfolio.getQuantity().add(request.getQuantity());
            BigDecimal newAvgPrice = newTotalCost.divide(newQuantity, 2, RoundingMode.HALF_UP);

            portfolio.setQuantity(newQuantity);
            portfolio.setAveragePurchasePrice(newAvgPrice);
            portfolio.setUpdatedAt(LocalDateTime.now());
            portfolioRepository.save(portfolio);

        } else {
            Portfolio portfolio = portfolioRepository.findByMemberIdAndSpotId(user.getId(), spot.getId())
                    .orElseThrow(() -> new CustomException("INSUFFICIENT_STOCK", "보유하고 있지 않은 관광지 주식은 매도할 수 없습니다."));

            if (portfolio.getQuantity().compareTo(request.getQuantity()) < 0) {
                throw new CustomException("INSUFFICIENT_STOCK", "매도하려는 수량이 보유 수량보다 많습니다.");
            }

            user.setBalance(user.getBalance().add(totalAmount));
            userRepository.save(user);

            BigDecimal remainingQuantity = portfolio.getQuantity().subtract(request.getQuantity());
            if (remainingQuantity.compareTo(BigDecimal.ZERO) == 0) {
                portfolioRepository.delete(portfolio);
            } else {
                portfolio.setQuantity(remainingQuantity);
                portfolio.setUpdatedAt(LocalDateTime.now());
                portfolioRepository.save(portfolio);
            }
        }

        Transaction transaction = Transaction.builder()
                .memberId(user.getId())
                .spotId(spot.getId())
                .type(type)
                .quantity(request.getQuantity())
                .price(spot.getCurrentPrice())
                .totalAmount(totalAmount)
                .executedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        log.info("가상 주식 체결 가동 완료 -> 유저: {}, 유형: {}, 종목: {}, 수량: {}, 체결가: {}", user.getNickname(), type, spot.getName(), request.getQuantity(), spot.getCurrentPrice());
        return transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public MemberAssetResponse getMemberAssets(Long memberId, String sort) {
        User user = userRepository.findById(memberId)
                .orElseThrow(() -> new CustomException("MEMBER_NOT_FOUND", "해당 사용자를 조회할 수 없습니다. ID: " + memberId));

        List<Portfolio> portfolios = portfolioRepository.findByMemberId(memberId);
        List<MemberAssetResponse.AssetItem> items = new ArrayList<>();

        BigDecimal totalStockValue = BigDecimal.ZERO;
        BigDecimal totalStockPurchaseCost = BigDecimal.ZERO;

        for (Portfolio p : portfolios) {
            Spot spot = spotRepository.findById(p.getSpotId()).orElse(null);
            if (spot == null) continue;

            BigDecimal evaluationAmount = spot.getCurrentPrice().multiply(p.getQuantity()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal purchaseCost = p.getAveragePurchasePrice().multiply(p.getQuantity()).setScale(2, RoundingMode.HALF_UP);

            totalStockValue = totalStockValue.add(evaluationAmount);
            totalStockPurchaseCost = totalStockPurchaseCost.add(purchaseCost);

            BigDecimal profitLossRate = BigDecimal.ZERO;
            if (p.getAveragePurchasePrice().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal diff = spot.getCurrentPrice().subtract(p.getAveragePurchasePrice());
                profitLossRate = diff.divide(p.getAveragePurchasePrice(), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
            }

            items.add(MemberAssetResponse.AssetItem.builder()
                    .spotId(spot.getId())
                    .spotName(spot.getName())
                    .quantity(p.getQuantity())
                    .averagePurchasePrice(p.getAveragePurchasePrice())
                    .currentPrice(spot.getCurrentPrice())
                    .evaluationAmount(evaluationAmount)
                    .profitLossRate(profitLossRate)
                    .build());
        }

        // 정렬 로직 적용
        if (sort != null) {
            switch (sort) {
                case "profit_rate":
                    items.sort((a, b) -> b.getProfitLossRate().compareTo(a.getProfitLossRate()));
                    break;
                case "eval_amount":
                    items.sort((a, b) -> b.getEvaluationAmount().compareTo(a.getEvaluationAmount()));
                    break;
                case "quantity":
                    items.sort((a, b) -> b.getQuantity().compareTo(a.getQuantity()));
                    break;
                default:
                    items.sort((a, b) -> b.getProfitLossRate().compareTo(a.getProfitLossRate()));
                    break;
            }
        }

        BigDecimal totalAssetValue = user.getBalance().add(totalStockValue);
        BigDecimal totalProfitLossRate = BigDecimal.ZERO;
        if (totalStockPurchaseCost.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = totalStockValue.subtract(totalStockPurchaseCost);
            totalProfitLossRate = diff.divide(totalStockPurchaseCost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        }

        return MemberAssetResponse.builder()
                .memberId(user.getId())
                .username(user.getNickname())
                .cashBalance(user.getBalance())
                .totalStockValue(totalStockValue)
                .totalAssetValue(totalAssetValue)
                .totalProfitLossRate(totalProfitLossRate)
                .items(items)
                .build();
    }

    @Transactional
    public void updateDailyStockPrices() {
        log.info("=== 관광 지표 기반 주가 정산 배치 시작 ===");

        // 캐시 초기화
        tourIndicatorService.clearCache();

        // KorService2 데이터 동기화 (이미지, GPS 좌표)
        syncKorService2Data();

        // 전체 종목 조회
        List<Spot> spots = spotRepository.findAll();
        log.info("전체 종목 수: {}", spots.size());

        // S는 전국 단일 계수이므로 배치당 한 번만 산출한다
        Double s = tourIndicatorService.collectS();

        int successCount = 0;
        int failureCount = 0;

        // 종목별 반복
        for (Spot spot : spots) {
            try {
                // 어제 컨텍스트 조회
                PriceCalculationService.YesterdayContext ctx = priceCalculationService.getYesterdayContext(spot);

                // 이전 지표값 (API 실패 시 폴백 소스). 이력이 없으면 중립값 0.5
                Double previousP = ctx != null && ctx.yesterdayP() != null ? ctx.yesterdayP() : 0.5;
                Double previousD = ctx != null && ctx.yesterdayD() != null ? ctx.yesterdayD() : 0.5;
                Double previousR = ctx != null && ctx.yesterdayR() != null ? ctx.yesterdayR() : 0.5;

                // 지표 수집 (시군구 단위 캐싱 활용)
                Double p = tourIndicatorService.collectP(spot, previousP);
                Double d = tourIndicatorService.collectD(spot.getAreaCode(), spot.getSignguCd(), previousD);
                Double r = tourIndicatorService.collectR(spot.getAreaCode(), spot.getSignguCd(), previousR);

                // 가격 계산
                BigDecimal newPrice = priceCalculationService.calculateTodayPrice(spot, ctx, p, d, r, s);

                // 변동률은 어제 종가(= 갱신 전 currentPrice) 대비로 계산한다
                BigDecimal yesterdayClose = spot.getCurrentPrice();
                BigDecimal changeRate = BigDecimal.ZERO;
                if (yesterdayClose.compareTo(BigDecimal.ZERO) > 0) {
                    changeRate = newPrice.subtract(yesterdayClose)
                            .divide(yesterdayClose, 4, RoundingMode.HALF_UP);
                }

                // TS 계산 (저장용)
                double todayTS = (p * 0.60) + (d * 0.25) + (r * 0.15);
                BigDecimal tsScore = BigDecimal.valueOf(todayTS);

                // 전날 종가(prevPrice) 갱신
                spot.setPrevPrice(yesterdayClose);
                spot.setCurrentPrice(newPrice);
                spot.setLastUpdated(LocalDateTime.now());
                spot.setTourismDataWeight(tsScore);

                spotRepository.save(spot);

                // price_history INSERT (지표 원값까지 함께 보존)
                savePriceHistory(spot, LocalDate.now(), newPrice, changeRate, tsScore, p, d, r, s);

                successCount++;
                log.info("주가 정산 완료: 종목={}, 어제={}, 오늘={}, 변동률={}%, P={}, D={}, R={}, S={}",
                        spot.getName(), yesterdayClose, newPrice,
                        changeRate.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP),
                        String.format("%.3f", p), String.format("%.3f", d),
                        String.format("%.3f", r), s);

            } catch (Exception e) {
                failureCount++;
                log.error("주가 정산 실패: spotId={}, name={}, error={}",
                        spot.getId(), spot.getName(), e.getMessage());
                // 실패 시 현재 주가를 그대로 유지 (난수 폴백 제거)
                applyPriceHoldFallback(spot);
            }
        }

        log.info("=== 관광 지표 기반 주가 정산 배치 완료 ===");
        log.info("📊 배치 결과: 성공 {}건, 실패 {}건 (총 {}건의 주가 이력이 price_history에 저장되었습니다.)",
                successCount, failureCount, successCount);
    }

    void savePriceHistory(Spot spot, LocalDate tradeDate, BigDecimal price, BigDecimal changeRate,
                          BigDecimal tsScore, Double p, Double d, Double r, Double s) {
        try {
            PriceHistory history = priceHistoryRepository.findBySpotIdAndTradeDate(spot.getId(), tradeDate);
            if (history == null) {
                history = PriceHistory.builder()
                        .spotId(spot.getId())
                        .tradeDate(tradeDate)
                        .createdAt(LocalDateTime.now())
                        .build();
            }
            history.setPrice(price);
            history.setChangeRate(changeRate);
            history.setTsScore(tsScore);
            history.setPScore(toBigDecimal(p));
            history.setDScore(toBigDecimal(d));
            history.setRScore(toBigDecimal(r));
            history.setSCoefficient(toBigDecimal(s));
            priceHistoryRepository.save(history);
        } catch (Exception e) {
            log.error("가격 이력 저장 실패: spotId={}, error={}", spot.getId(), e.getMessage());
        }
    }

    private static BigDecimal toBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }

    // 폴백: API 실패 시 현재 주가를 그대로 유지 (난수 제거)
    private void applyPriceHoldFallback(Spot spot) {
        BigDecimal currentPrice = spot.getCurrentPrice();
        BigDecimal changeRate = BigDecimal.ZERO;

        spot.setPrevPrice(currentPrice);
        spot.setCurrentPrice(currentPrice);
        spot.setLastUpdated(LocalDateTime.now());
        spotRepository.save(spot);

        savePriceHistory(spot, LocalDate.now(), currentPrice, changeRate,
                spot.getTourismDataWeight(), null, null, null, null);

        log.info("주가 유지 폴백 적용: 종목={}, 가격={} (변동 없음)",
                spot.getName(), currentPrice);
    }


    private BigDecimal calculateNewPrice(BigDecimal currentPrice, BigDecimal finalChangeRate) {
        return currentPrice.add(currentPrice.multiply(finalChangeRate)).setScale(0, RoundingMode.HALF_UP);
    }

    private String getImageUrlWithFallback(Spot spot) {
        return spot.getImageUrl() != null && !spot.getImageUrl().isEmpty() ? spot.getImageUrl() : "https://via.placeholder.com/800x600?text=No+Image";
    }

    /**
     * KorService2 API를 통해 관광지 이미지 및 GPS 좌표 동기화
     * 서버 기동 시 자동 실행
     */
    @EventListener(ApplicationReadyEvent.class)
    public void syncKorService2Data() {
        log.info("KorService2 데이터 동기화 시작");
        List<Spot> spots = spotRepository.findAll();
        int updatedCount = 0;

        for (Spot spot : spots) {
            try {
                // 이미지와 좌표가 모두 있는 경우 건너뜀
                if (spot.getImageUrl() != null && !spot.getImageUrl().isEmpty()
                        && spot.getMapX() != null && !spot.getMapX().isEmpty()
                        && spot.getMapY() != null && !spot.getMapY().isEmpty()) {
                    continue;
                }

                KorService2Dto dto = korService2Client.fetchDetailCommon(spot.getContentId());
                if (dto != null) {
                    boolean updated = false;
                    // 이미지 URL 업데이트 (API 응답이 있으면 무조건 업데이트)
                    if (dto.getFirstImage() != null && !dto.getFirstImage().isEmpty()) {
                        spot.setImageUrl(dto.getFirstImage());
                        updated = true;
                    }
                    // GPS 좌표 업데이트 (API 응답이 있으면 무조건 업데이트)
                    if (dto.getMapX() != null && !dto.getMapX().isEmpty()) {
                        spot.setMapX(dto.getMapX());
                        updated = true;
                    }
                    if (dto.getMapY() != null && !dto.getMapY().isEmpty()) {
                        spot.setMapY(dto.getMapY());
                        updated = true;
                    }
                    // 상세 주소 업데이트 (API 응답이 있으면 무조건 업데이트)
                    if (dto.getAddr1() != null && !dto.getAddr1().isEmpty()) {
                        spot.setAddress(dto.getAddr1());
                        updated = true;
                    }
                    // 카테고리 기반 태그 생성 (API 응답이 있으면 무조건 업데이트)
                    String generatedTags = generateTagsFromCategories(dto, spot);
                    if (generatedTags != null && !generatedTags.isEmpty()) {
                        spot.setThemeTag(generatedTags);
                        updated = true;
                    }
                    // 개요/설명 업데이트 (API 응답이 있으면 무조건 업데이트)
                    if (dto.getOverview() != null && !dto.getOverview().isEmpty()) {
                        spot.setDescription(dto.getOverview());
                        updated = true;
                    }
                    if (updated) {
                        spotRepository.save(spot);
                        updatedCount++;
                        log.info("KorService2 데이터 강제 업데이트: spotId={}, name={}, contentId={}, imageUrl={}, mapX={}, mapY={}, address={}, themeTag={}, description={}",
                                spot.getId(), spot.getName(), spot.getContentId(), spot.getImageUrl(), spot.getMapX(), spot.getMapY(), spot.getAddress(), spot.getThemeTag(), spot.getDescription());
                    } else {
                        log.warn("KorService2 응답 데이터 없음: spotId={}, name={}, contentId={}", spot.getId(), spot.getName(), spot.getContentId());
                    }
                }
            } catch (Exception e) {
                log.warn("KorService2 동기화 실패: spotId={}, name={}, error={}",
                        spot.getId(), spot.getName(), e.getMessage());
            }
        }
        log.info("KorService2 데이터 동기화 완료: {}건 업데이트", updatedCount);
    }

    private String generateTagsFromCategories(KorService2Dto dto, Spot spot) {
        java.util.List<String> tags = new java.util.ArrayList<>();

        // 카테고리에서 태그 추출
        if (dto.getCat1() != null && !dto.getCat1().isEmpty()) {
            tags.add(dto.getCat1().trim());
        }
        if (dto.getCat2() != null && !dto.getCat2().isEmpty()) {
            tags.add(dto.getCat2().trim());
        }
        if (dto.getCat3() != null && !dto.getCat3().isEmpty()) {
            tags.add(dto.getCat3().trim());
        }

        // 지역명 추가
        if (spot.getRegion() != null && !spot.getRegion().isEmpty()) {
            tags.add(spot.getRegion().trim());
        }

        // 테마 추가
        if (spot.getTheme() != null && !spot.getTheme().isEmpty()) {
            tags.add(spot.getTheme().trim());
        }

        // 중복 제거 및 쉼표로 구분된 문자열 반환
        return tags.stream().distinct().collect(java.util.stream.Collectors.joining(","));
    }

    public List<StockResponse> getAllStocks() {
        return spotRepository.findAllByOrderByTierAscNameAsc().stream()
                .map(this::mapToStockResponse)
                .toList();
    }

    private StockResponse mapToStockResponse(Spot spot) {
        BigDecimal changeRate = BigDecimal.ZERO;
        if (spot.getPrevPrice().compareTo(BigDecimal.ZERO) > 0) {
            changeRate = spot.getCurrentPrice().subtract(spot.getPrevPrice())
                    .divide(spot.getPrevPrice(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        }
        return StockResponse.builder()
                .id(spot.getId())
                .name(spot.getName())
                .areaCode(spot.getAreaCode())
                .regionName(spot.getAreaName() != null ? spot.getAreaName() : spot.getRegion())
                .tier(spot.getTier())
                .currentPrice(spot.getCurrentPrice())
                .prevPrice(spot.getPrevPrice())
                .changeRate(changeRate)
                .lastUpdated(spot.getLastUpdated())
                .imageUrl(getImageUrlWithFallback(spot))
                .mapX(spot.getMapX())
                .mapY(spot.getMapY())
                .build();
    }

    public List<StockResponse> getTopGainers() {
        List<Spot> spots = spotRepository.findAllOrderByChangeRateDesc();
        return spots.stream()
                .limit(3)
                .map(this::mapToStockResponse)
                .collect(Collectors.toList());
    }

    public List<StockResponse> getTopLosers() {
        List<Spot> spots = spotRepository.findAllOrderByChangeRateAsc();
        return spots.stream()
                .limit(3)
                .map(this::mapToStockResponse)
                .collect(Collectors.toList());
    }

    public List<RegionalIndexResponse> getRegionalIndex() {
        List<Spot> allSpots = spotRepository.findAll();
        Map<String, List<Spot>> spotsByRegion = allSpots.stream()
                .collect(Collectors.groupingBy(Spot::getRegion));

        List<RegionalIndexResponse> regionalIndices = new ArrayList<>();
        for (Map.Entry<String, List<Spot>> entry : spotsByRegion.entrySet()) {
            String region = entry.getKey();
            List<Spot> regionSpots = entry.getValue();

            BigDecimal totalChangeRate = BigDecimal.ZERO;
            for (Spot spot : regionSpots) {
                BigDecimal changeRate = BigDecimal.ZERO;
                if (spot.getPrevPrice().compareTo(BigDecimal.ZERO) > 0) {
                    changeRate = spot.getCurrentPrice().subtract(spot.getPrevPrice())
                            .divide(spot.getPrevPrice(), 4, RoundingMode.HALF_UP);
                }
                totalChangeRate = totalChangeRate.add(changeRate);
            }

            BigDecimal averageChangeRate = totalChangeRate.divide(
                    BigDecimal.valueOf(regionSpots.size()), 4, RoundingMode.HALF_UP);

            regionalIndices.add(RegionalIndexResponse.builder()
                    .region(region)
                    .averageChangeRate(averageChangeRate.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP))
                    .spotCount((long) regionSpots.size())
                    .build());
        }

        return regionalIndices.stream()
                .sorted(Comparator.comparing(RegionalIndexResponse::getAverageChangeRate).reversed())
                .collect(Collectors.toList());
    }

    public List<StockResponse> searchStocks(String region, String theme, String sort) {
        List<Spot> spots;

        switch (sort) {
            case "price":
                spots = spotRepository.findByRegionAndThemeOrderByPriceDesc(region, theme);
                break;
            case "price_asc":
                spots = spotRepository.findByRegionAndThemeOrderByPriceAsc(region, theme);
                break;
            case "change_rate":
                spots = spotRepository.findByRegionAndThemeOrderByChangeRateDesc(region, theme);
                break;
            case "change_rate_asc":
                spots = spotRepository.findByRegionAndThemeOrderByChangeRateAsc(region, theme);
                break;
            case "tier":
                spots = spotRepository.findByRegionAndThemeOrderByTierAsc(region, theme);
                break;
            default:
                spots = spotRepository.findByRegionAndTheme(region, theme);
                break;
        }

        return spots.stream()
                .map(this::mapToStockResponse)
                .collect(Collectors.toList());
    }

    public List<StockResponse> searchStocksUnified(String region, String keyword, List<String> tags, String sortBy, String sortOrder) {
        List<Spot> spots;
        String normalizedRegion = ("ALL".equals(region) || "전체".equals(region) || region == null || region.trim().isEmpty()) ? null : region;
        String normalizedKeyword = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
        List<String> normalizedTags = (tags == null || tags.isEmpty()) ? null : tags;
        String normalizedSortBy = (sortBy == null || sortBy.trim().isEmpty()) ? "changeRate" : sortBy;
        String normalizedSortOrder = (sortOrder == null || sortOrder.trim().isEmpty()) ? "DESC" : sortOrder.toUpperCase();

        // 기본 조회
        if (normalizedKeyword != null) {
            // 키워드 검색: 이름, 지역명, 태그명 검색
            spots = spotRepository.findAll().stream()
                    .filter(spot -> normalizedRegion == null || normalizedRegion.equals(spot.getRegion()) || normalizedRegion.equals(spot.getAreaCode()))
                    .filter(spot -> normalizedTags == null || normalizedTags.isEmpty() || (spot.getThemeTag() != null && normalizedTags.stream()
                            .anyMatch(tag -> spot.getThemeTag().toLowerCase().contains(tag.toLowerCase()))))
                    .filter(spot -> spot.getName().toLowerCase().contains(normalizedKeyword.toLowerCase()) ||
                                   (spot.getRegion() != null && spot.getRegion().toLowerCase().contains(normalizedKeyword.toLowerCase())) ||
                                   (spot.getAreaName() != null && spot.getAreaName().toLowerCase().contains(normalizedKeyword.toLowerCase())) ||
                                   (spot.getThemeTag() != null && spot.getThemeTag().toLowerCase().contains(normalizedKeyword.toLowerCase())))
                    .collect(Collectors.toList());
        } else {
            // 지역 필터만 적용
            if (normalizedRegion != null) {
                spots = spotRepository.findByRegion(normalizedRegion);
            } else {
                spots = spotRepository.findAll();
            }
            // 다중 태그 필터 적용 (OR 조건)
            if (normalizedTags != null && !normalizedTags.isEmpty()) {
                spots = spots.stream()
                        .filter(spot -> spot.getThemeTag() != null && normalizedTags.stream()
                                .anyMatch(tag -> spot.getThemeTag().toLowerCase().contains(tag.toLowerCase())))
                        .collect(Collectors.toList());
            }
        }

        // 정렬 적용
        switch (normalizedSortBy) {
            case "price":
                spots = "DESC".equals(normalizedSortOrder) ?
                        spots.stream().sorted(Comparator.comparing(Spot::getCurrentPrice).reversed()).collect(Collectors.toList()) :
                        spots.stream().sorted(Comparator.comparing(Spot::getCurrentPrice)).collect(Collectors.toList());
                break;
            case "changeRate":
                spots = "DESC".equals(normalizedSortOrder) ?
                        spots.stream().sorted(Comparator.comparing((Spot s) -> calculateChangeRate(s)).reversed()).collect(Collectors.toList()) :
                        spots.stream().sorted(Comparator.comparing(this::calculateChangeRate)).collect(Collectors.toList());
                break;
            case "name":
                spots = "DESC".equals(normalizedSortOrder) ?
                        spots.stream().sorted(Comparator.comparing(Spot::getName).reversed()).collect(Collectors.toList()) :
                        spots.stream().sorted(Comparator.comparing(Spot::getName)).collect(Collectors.toList());
                break;
            case "tier":
                spots = "DESC".equals(normalizedSortOrder) ?
                        spots.stream().sorted(Comparator.comparing(Spot::getTier).reversed()).collect(Collectors.toList()) :
                        spots.stream().sorted(Comparator.comparing(Spot::getTier)).collect(Collectors.toList());
                break;
            default:
                // 기본: changeRate DESC
                spots = spots.stream().sorted(Comparator.comparing((Spot s) -> calculateChangeRate(s)).reversed()).collect(Collectors.toList());
                break;
        }

        return spots.stream()
                .map(this::mapToStockResponse)
                .collect(Collectors.toList());
    }

    private BigDecimal calculateChangeRate(Spot spot) {
        if (spot.getPrevPrice().compareTo(BigDecimal.ZERO) > 0) {
            return spot.getCurrentPrice().subtract(spot.getPrevPrice())
                    .divide(spot.getPrevPrice(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    public List<PriceHistoryResponse> getPriceHistory(Long spotId, String period) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = switch (period) {
            case "1w" -> endDate.minusWeeks(1);
            case "1m" -> endDate.minusMonths(1);
            case "3m" -> endDate.minusMonths(3);
            case "all" -> LocalDate.of(2000, 1, 1);
            default -> endDate.minusWeeks(1);
        };

        List<PriceHistory> histories;
        if (period.equals("all")) {
            histories = priceHistoryRepository.findBySpotIdOrderByTradeDateAsc(spotId);
        } else {
            histories = priceHistoryRepository.findBySpotIdAndTradeDateBetweenOrderByTradeDateAsc(
                    spotId, startDate, endDate);
        }

        return histories.stream()
                .map(ph -> PriceHistoryResponse.builder()
                        .tradeDate(ph.getTradeDate())
                        .price(ph.getPrice())
                        .changeRate(ph.getChangeRate().multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP))
                        .tsScore(ph.getTsScore())
                        .build())
                .collect(Collectors.toList());
    }

    public List<StockChartResponse> getStockChart(Long spotId, String period) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = switch (period.toUpperCase()) {
            case "1W" -> endDate.minusDays(7);
            case "3M" -> endDate.minusDays(90);
            case "1Y" -> endDate.minusDays(365);
            case "5Y" -> endDate.minusDays(1825);
            case "ALL" -> LocalDate.of(2000, 1, 1);
            default -> endDate.minusDays(7);
        };

        List<PriceHistory> histories;
        if (period.toUpperCase().equals("ALL")) {
            histories = priceHistoryRepository.findBySpotIdOrderByTradeDateAsc(spotId);
        } else {
            histories = priceHistoryRepository.findBySpotIdAndTradeDateBetweenOrderByTradeDateAsc(
                    spotId, startDate, endDate);
        }

        return histories.stream()
                .map(ph -> StockChartResponse.builder()
                        .date(ph.getTradeDate())
                        .price(ph.getPrice())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PortfolioSummaryResponse getPortfolioSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("USER_NOT_FOUND", "해당 사용자를 조회할 수 없습니다. ID: " + userId));

        List<Portfolio> portfolios = portfolioRepository.findByMemberId(userId);

        BigDecimal totalEvaluation = BigDecimal.ZERO;
        BigDecimal totalPurchase = BigDecimal.ZERO;

        for (Portfolio p : portfolios) {
            Spot spot = spotRepository.findById(p.getSpotId()).orElse(null);
            if (spot == null) continue;

            BigDecimal evaluationAmount = spot.getCurrentPrice().multiply(p.getQuantity()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal purchaseCost = p.getAveragePurchasePrice().multiply(p.getQuantity()).setScale(2, RoundingMode.HALF_UP);

            totalEvaluation = totalEvaluation.add(evaluationAmount);
            totalPurchase = totalPurchase.add(purchaseCost);
        }

        BigDecimal totalAsset = user.getBalance().add(totalEvaluation);
        BigDecimal totalProfitLoss = totalEvaluation.subtract(totalPurchase);
        BigDecimal profitRate = BigDecimal.ZERO;
        if (totalPurchase.compareTo(BigDecimal.ZERO) > 0) {
            profitRate = totalProfitLoss.divide(totalPurchase, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        }

        // 자산 추이 데이터 생성 (최근 7일)
        List<PortfolioSummaryResponse.AssetHistoryItem> assetHistory = generateAssetHistory(userId, totalAsset);

        return PortfolioSummaryResponse.builder()
                .totalAsset(totalAsset)
                .totalEvaluation(totalEvaluation)
                .totalPurchase(totalPurchase)
                .totalProfitLoss(totalProfitLoss)
                .profitRate(profitRate)
                .cashBalance(user.getBalance())
                .assetHistory(assetHistory)
                .build();
    }

    private List<PortfolioSummaryResponse.AssetHistoryItem> generateAssetHistory(Long userId, BigDecimal currentTotalAsset) {
        List<PortfolioSummaryResponse.AssetHistoryItem> history = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // 최근 7일 데이터 생성 (실제 데이터가 없으면 현재 자산으로 대체)
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String formattedDate = String.format("%02d/%02d", date.getMonthValue(), date.getDayOfMonth());

            // 실제 자산 이력 데이터가 있다면 조회, 없으면 현재 자산 사용
            // TODO: 자산 이력 테이블이 구현되면 실제 데이터 조회 로직 추가
            // 현재는 임시로 현재 자산으로 반환
            history.add(PortfolioSummaryResponse.AssetHistoryItem.builder()
                    .date(formattedDate)
                    .totalAsset(currentTotalAsset)
                    .build());
        }

        return history;
    }

    @Transactional(readOnly = true)
    public List<StockResponse> getTrendingStocks() {
        List<Spot> allSpots = spotRepository.findAll();
        // 등락률 기준 상위 10개 종목 반환
        return allSpots.stream()
                .sorted(Comparator.comparing((Spot s) -> calculateChangeRate(s)).reversed())
                .limit(10)
                .map(this::mapToStockResponse)
                .collect(Collectors.toList());
    }
}