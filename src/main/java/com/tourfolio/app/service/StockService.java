// src/main/java/com/tourfolio/app/service/StockService.java
package com.tourfolio.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.tourfolio.app.dto.StockResponse;
import com.tourfolio.app.dto.TradeRequest;
import com.tourfolio.app.dto.MemberAssetResponse;
import com.tourfolio.app.dto.PriceHistoryResponse;
import com.tourfolio.app.dto.RegionalIndexResponse;
import com.tourfolio.app.entity.Spot;
import com.tourfolio.app.entity.Transaction;
import com.tourfolio.app.entity.Member;
import com.tourfolio.app.entity.Portfolio;
import com.tourfolio.app.entity.PriceHistory;
import com.tourfolio.app.exception.CustomException;
import com.tourfolio.app.repository.SpotRepository;
import com.tourfolio.app.repository.TransactionRepository;
import com.tourfolio.app.repository.MemberRepository;
import com.tourfolio.app.repository.PortfolioRepository;
import com.tourfolio.app.repository.PriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final MemberRepository memberRepository;
    private final PortfolioRepository portfolioRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final OpenApiService openApiService;
    private final TourIndicatorService tourIndicatorService;
    private final PriceCalculationService priceCalculationService;

    @Transactional(rollbackFor = Exception.class)
    public Transaction executeTrade(TradeRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
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
            if (member.getBalance().compareTo(totalAmount) < 0) {
                throw new CustomException("INSUFFICIENT_BALANCE", "보유 포인트 잔액이 부족하여 가상 매수가 불가능합니다.");
            }
            member.setBalance(member.getBalance().subtract(totalAmount));
            memberRepository.save(member);

            // 포트폴리오 원장 갱신 및 평균 매수 평단가 가중치 계산
            Portfolio portfolio = portfolioRepository.findByMemberIdAndSpotId(member.getId(), spot.getId())
                    .orElse(Portfolio.builder()
                            .memberId(member.getId())
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
            Portfolio portfolio = portfolioRepository.findByMemberIdAndSpotId(member.getId(), spot.getId())
                    .orElseThrow(() -> new CustomException("INSUFFICIENT_STOCK", "보유하고 있지 않은 관광지 주식은 매도할 수 없습니다."));

            if (portfolio.getQuantity().compareTo(request.getQuantity()) < 0) {
                throw new CustomException("INSUFFICIENT_STOCK", "매도하려는 수량이 보유 수량보다 많습니다.");
            }

            member.setBalance(member.getBalance().add(totalAmount));
            memberRepository.save(member);

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
                .memberId(member.getId())
                .spotId(spot.getId())
                .type(type)
                .quantity(request.getQuantity())
                .price(spot.getCurrentPrice())
                .totalAmount(totalAmount)
                .executedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        log.info("가상 주식 체결 가동 완료 -> 유저: {}, 유형: {}, 종목: {}, 수량: {}, 체결가: {}", member.getNickname(), type, spot.getName(), request.getQuantity(), spot.getCurrentPrice());
        return transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public MemberAssetResponse getMemberAssets(Long memberId, String sort) {
        Member member = memberRepository.findById(memberId)
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

        BigDecimal totalAssetValue = member.getBalance().add(totalStockValue);
        BigDecimal totalProfitLossRate = BigDecimal.ZERO;
        if (totalStockPurchaseCost.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = totalStockValue.subtract(totalStockPurchaseCost);
            totalProfitLossRate = diff.divide(totalStockPurchaseCost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        }

        return MemberAssetResponse.builder()
                .memberId(member.getId())
                .username(member.getNickname())
                .cashBalance(member.getBalance())
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
        
        // 전체 종목 조회
        List<Spot> spots = spotRepository.findAll();
        log.info("전체 종목 수: {}", spots.size());
        
        // 광역시도 코드 리스트 추출 (S계수 사전 계산용)
        List<String> areaCodes = spots.stream()
                .map(Spot::getAreaCode)
                .distinct()
                .toList();
        
        // S계수 사전 계산 (1회)
        tourIndicatorService.initializeSCache(areaCodes);
        
        // 종목별 반복
        for (Spot spot : spots) {
            try {
                // 어제 컨텍스트 조회
                PriceCalculationService.YesterdayContext ctx = priceCalculationService.getYesterdayContext(spot);
                
                // 이전 지표값 (폴백용)
                Double previousP = ctx != null ? ctx.getYesterdayTS() : 0.5;
                Double previousD = 0.5;
                Double previousR = 0.5;
                
                // 지표 수집 (P/D/R 수집, 캐싱 활용)
                Double p = tourIndicatorService.collectP(spot, previousP);
                Double d = tourIndicatorService.collectD(spot.getAreaCode(), spot.getSignguCd(), previousD);
                Double r = tourIndicatorService.collectR(spot.getAreaCode(), spot.getSignguCd(), previousR);
                Double s = tourIndicatorService.collectS(spot.getAreaCode());
                
                // 가격 계산
                BigDecimal newPrice = priceCalculationService.calculateTodayPrice(spot, ctx, p, d, r, s);
                
                // 변동률 계산
                BigDecimal changeRate = BigDecimal.ZERO;
                if (spot.getPrevPrice().compareTo(BigDecimal.ZERO) > 0) {
                    changeRate = newPrice.subtract(spot.getPrevPrice())
                            .divide(spot.getPrevPrice(), 4, RoundingMode.HALF_UP);
                }
                
                // TS 계산 (저장용)
                double todayTS = (p * 0.60) + (d * 0.25) + (r * 0.15);
                BigDecimal tsScore = BigDecimal.valueOf(todayTS);
                
                // 전날 종가(prevPrice) 갱신
                spot.setPrevPrice(spot.getCurrentPrice());
                spot.setCurrentPrice(newPrice);
                spot.setLastUpdated(LocalDateTime.now());
                spot.setTourismDataWeight(tsScore);
                
                spotRepository.save(spot);
                
                // price_history INSERT
                savePriceHistory(spot, newPrice, changeRate, tsScore);
                
                log.info("주가 정산 완료: 종목={}, 어제가격={}, 오늘가격={}, 변동률={}%, P={}, D={}, R={}, S={}",
                        spot.getName(), spot.getPrevPrice(), newPrice, 
                        changeRate.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP),
                        String.format("%.3f", p), String.format("%.3f", d), 
                        String.format("%.3f", r), s);
                        
            } catch (Exception e) {
                log.error("주가 정산 실패: spotId={}, name={}, error={}", 
                        spot.getId(), spot.getName(), e.getMessage());
                // 실패 시 가우시안 랜덤워크 폴백
                applyGaussianRandomWalkFallback(spot);
            }
        }
        
        log.info("=== 관광 지표 기반 주가 정산 배치 완료 ===");
    }

    private void savePriceHistory(Spot spot, BigDecimal price, BigDecimal changeRate, BigDecimal tsScore) {
        LocalDate today = LocalDate.now();
        try {
            PriceHistory existingHistory = priceHistoryRepository.findBySpotIdAndTradeDate(spot.getId(), today);
            if (existingHistory != null) {
                existingHistory.setPrice(price);
                existingHistory.setChangeRate(changeRate);
                existingHistory.setTsScore(tsScore);
                priceHistoryRepository.save(existingHistory);
            } else {
                PriceHistory newHistory = PriceHistory.builder()
                        .spotId(spot.getId())
                        .tradeDate(today)
                        .price(price)
                        .changeRate(changeRate)
                        .tsScore(tsScore)
                        .createdAt(LocalDateTime.now())
                        .build();
                priceHistoryRepository.save(newHistory);
            }
        } catch (Exception e) {
            log.error("가격 이력 저장 실패: spotId={}, error={}", spot.getId(), e.getMessage());
        }
    }

    private BigDecimal calculateTodayTourismScore(Spot spot) {
        try {
            String apiResponse = openApiService.fetchDetailInfo(spot.getContentId());
            JsonNode rootNode = openApiService.parseJsonResponse(apiResponse);
            JsonNode items = openApiService.extractItems(rootNode);

            if (items.size() > 0) {
                JsonNode item = items.get(0);
                
                BigDecimal popularity = extractBigDecimal(item, "readcount");
                BigDecimal stayDuration = extractBigDecimal(item, "staytime");
                BigDecimal spending = extractBigDecimal(item, "spendmoney");
                BigDecimal serviceQuality = extractBigDecimal(item, "servicequality");
                BigDecimal cultureIndex = extractBigDecimal(item, "cultureindex");

                BigDecimal pNormalized = openApiService.normalizePopularity(popularity);
                BigDecimal dStayNormalized = openApiService.normalizeStayDuration(stayDuration);
                BigDecimal dSpendNormalized = openApiService.normalizeSpending(spending);
                BigDecimal rServiceNormalized = openApiService.normalizeServiceQuality(serviceQuality);
                BigDecimal rCultureNormalized = openApiService.normalizeCultureIndex(cultureIndex);

                BigDecimal dNormalized = dStayNormalized.add(dSpendNormalized).divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
                BigDecimal rNormalized = rServiceNormalized.add(rCultureNormalized).divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);

                return openApiService.calculateTourismScore(pNormalized, dNormalized, rNormalized);
            } else {
                log.warn("API 응답에 데이터가 없어서 기본값 사용: spotId={}", spot.getId());
                return calculateFallbackTourismScore(spot);
            }
        } catch (Exception e) {
            log.error("관광 데이터 조회 실패로 폴백 사용: spotId={}, error={}", spot.getId(), e.getMessage());
            return calculateFallbackTourismScore(spot);
        }
    }

    private BigDecimal extractBigDecimal(JsonNode node, String fieldName) {
        try {
            if (node.has(fieldName) && !node.get(fieldName).isNull()) {
                String value = node.get(fieldName).asText();
                if (value != null && !value.isEmpty()) {
                    return new BigDecimal(value);
                }
            }
        } catch (Exception e) {
            log.debug("필드 추출 실패: fieldName={}", fieldName);
        }
        return null;
    }

    private BigDecimal calculateYesterdayTourismScore(Spot spot) {
        BigDecimal baseWeight = spot.getTourismDataWeight();
        if (baseWeight == null || baseWeight.compareTo(BigDecimal.ZERO) == 0) {
            baseWeight = BigDecimal.valueOf(0.5);
        }
        return baseWeight;
    }

    private BigDecimal calculateTourismChangeRate(BigDecimal todayScore, BigDecimal yesterdayScore) {
        if (yesterdayScore.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return todayScore.subtract(yesterdayScore).divide(yesterdayScore, 4, RoundingMode.HALF_UP);
    }

    // US(User Sentiment) = (매수량 - 매도량) / 전체거래량 (최근 하루치)
    private BigDecimal calculateUserSentiment(Spot spot, LocalDateTime startTime) {
        List<Transaction> recentTransactions = transactionRepository.findBySpotIdAndCreatedAtAfterOrderByCreatedAtAsc(spot.getId(), startTime);
        if (recentTransactions.isEmpty()) return BigDecimal.ZERO;

        BigDecimal buyVolume = BigDecimal.ZERO;
        BigDecimal sellVolume = BigDecimal.ZERO;

        for (Transaction tx : recentTransactions) {
            if ("BUY".equals(tx.getType())) buyVolume = buyVolume.add(tx.getQuantity());
            else if ("SELL".equals(tx.getType())) sellVolume = sellVolume.add(tx.getQuantity());
        }

        BigDecimal totalVolume = buyVolume.add(sellVolume);
        if (totalVolume.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

        return buyVolume.subtract(sellVolume).divide(totalVolume, 4, RoundingMode.HALF_UP);
    }

    // S(빅데이터 보정계수) = 0.9 ~ 1.2 가중치 (관광 데이터 트렌드 기반)
    private BigDecimal calculateBigDataCoefficient(Spot spot) {
        // 현재는 랜덤 가중치 적용 (향후 빅데이터 API 연동 가능)
        double baseCoefficient = 0.9 + (Math.random() * 0.3); // 0.9 ~ 1.2
        return BigDecimal.valueOf(baseCoefficient).setScale(2, RoundingMode.HALF_UP);
    }

    // 최종 변동률 (FinalChange) = (TS_change * 0.8 + US * 0.2) * S
    private BigDecimal calculateFinalChangeRate(BigDecimal tsChangeRate, BigDecimal userSentiment, BigDecimal bigDataCoefficient) {
        BigDecimal weightedChange = tsChangeRate.multiply(BigDecimal.valueOf(0.8))
                .add(userSentiment.multiply(BigDecimal.valueOf(0.2)));
        return weightedChange.multiply(bigDataCoefficient).setScale(4, RoundingMode.HALF_UP);
    }

    // 상하한가 30% 제한 룰 적용
    private BigDecimal applyPriceLimit30Percent(BigDecimal changeRate) {
        BigDecimal maxChange = BigDecimal.valueOf(0.30);
        BigDecimal minChange = BigDecimal.valueOf(-0.30);
        
        if (changeRate.compareTo(maxChange) > 0) {
            log.debug("상한가 30% 도달로 변동률 제한: 원본={}, 제한={}", changeRate, maxChange);
            return maxChange;
        }
        if (changeRate.compareTo(minChange) < 0) {
            log.debug("하한가 30% 도달로 변동률 제한: 원본={}, 제한={}", changeRate, minChange);
            return minChange;
        }
        return changeRate;
    }

    // 가우시안 랜덤워크 폴백: -5% ~ +5% 내외로 자연스럽게 상시 변동
    private void applyGaussianRandomWalkFallback(Spot spot) {
        // 가우시안 분포 기반 랜덤 변동률 생성 (평균 0, 표준편차 0.02)
        java.util.Random random = new java.util.Random();
        double gaussian = random.nextGaussian();
        double changeRate = gaussian * 0.02; // 약 ±5% 범위
        
        // -5% ~ +5% 범위 제한
        if (changeRate > 0.05) changeRate = 0.05;
        if (changeRate < -0.05) changeRate = -0.05;
        
        BigDecimal randomChangeRate = BigDecimal.valueOf(changeRate).setScale(4, RoundingMode.HALF_UP);
        BigDecimal newPrice = calculateNewPrice(spot.getCurrentPrice(), randomChangeRate);

        if (newPrice.compareTo(BigDecimal.valueOf(100)) < 0) {
            newPrice = BigDecimal.valueOf(100);
        }

        BigDecimal changeRateForHistory = BigDecimal.ZERO;
        if (spot.getPrevPrice().compareTo(BigDecimal.ZERO) > 0) {
            changeRateForHistory = newPrice.subtract(spot.getPrevPrice())
                    .divide(spot.getPrevPrice(), 4, RoundingMode.HALF_UP);
        }

        spot.setPrevPrice(spot.getCurrentPrice());
        spot.setCurrentPrice(newPrice);
        spot.setLastUpdated(LocalDateTime.now());
        spotRepository.save(spot);

        savePriceHistory(spot, newPrice, changeRateForHistory, spot.getTourismDataWeight());

        log.info("가우시안 랜덤워크 폴백 엔진 구동 -> 종목: {}, 랜덤 변동률: {}%, 가격: {} -> {}",
                spot.getName(), randomChangeRate.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP),
                spot.getPrevPrice(), spot.getCurrentPrice());
    }


    private BigDecimal calculateNewPrice(BigDecimal currentPrice, BigDecimal finalChangeRate) {
        return currentPrice.add(currentPrice.multiply(finalChangeRate)).setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateFallbackTourismScore(Spot spot) {
        String theme = spot.getTheme() != null ? spot.getTheme() : "자연";
        String region = spot.getRegion() != null ? spot.getRegion() : "서울";
        
        log.warn("API 실패로 테마/지역 기반 Fallback 점수 계산: spotId={}, theme={}, region={}", 
                spot.getId(), theme, region);
        
        return openApiService.calculateFallbackTourismScore(theme, region);
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
                .tier(spot.getTier())
                .currentPrice(spot.getCurrentPrice())
                .prevPrice(spot.getPrevPrice())
                .changeRate(changeRate)
                .lastUpdated(spot.getLastUpdated())
                .build();
    }

    public Map<String, Object> testOpenApiMetrics(Long spotId) {
        Spot spot = spotRepository.findById(spotId).orElseThrow(() -> new IllegalArgumentException("Spot not found"));
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("spotName", spot.getName());
        metrics.put("tourismScore", spot.getTourismDataWeight());
        metrics.put("timestamp", LocalDateTime.now().toString());
        return metrics;
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

    public List<PriceHistoryResponse> getPriceHistory(Long spotId, String period) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;

        switch (period) {
            case "1w":
                startDate = endDate.minusWeeks(1);
                break;
            case "1m":
                startDate = endDate.minusMonths(1);
                break;
            case "3m":
                startDate = endDate.minusMonths(3);
                break;
            case "all":
                startDate = LocalDate.of(2000, 1, 1);
                break;
            default:
                startDate = endDate.minusWeeks(1);
                break;
        }

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
}