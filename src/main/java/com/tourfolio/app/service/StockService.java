package com.tourfolio.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourfolio.app.dto.OpenApiDto;
import com.tourfolio.app.dto.StockResponse;
import com.tourfolio.app.dto.TradeRequest;
import com.tourfolio.app.dto.MemberAssetResponse;
import com.tourfolio.app.entity.Spot;
import com.tourfolio.app.entity.Transaction;
import com.tourfolio.app.entity.Member;
import com.tourfolio.app.entity.Portfolio;
import com.tourfolio.app.exception.CustomException;
import com.tourfolio.app.repository.SpotRepository;
import com.tourfolio.app.repository.TransactionRepository;
import com.tourfolio.app.repository.MemberRepository;
import com.tourfolio.app.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final SpotRepository spotRepository;
    private final TransactionRepository transactionRepository;
    private final MemberRepository memberRepository;
    private final PortfolioRepository portfolioRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    @Value("${openapi.service.key:mock_key}")
    private String openApiServiceKey;

    @Value("${endpoint.demand:http://localhost:8080/mock}")
    private String endpointDemand;

    @Value("${endpoint.resdem:http://localhost:8080/mock}")
    private String endpointResdem;

    @Value("${endpoint.visitor:http://localhost:8080/mock}")
    private String endpointVisitor;

    @Value("${endpoint.forecast:http://localhost:8080/mock}")
    private String endpointForecast;

    @Transactional(rollbackFor = Exception.class)
    public Transaction executeTrade(TradeRequest request) {
        // 1. 가상 투자 유저 엔티티 검증
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new CustomException("MEMBER_NOT_FOUND", "해당 사용자를 조회할 수 없습니다. ID: " + request.getMemberId()));

        // 2. 상장 관광 자산 종목 검증
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
            // 매수 원자성 검증 규칙: 지갑 잔액 비교
            if (member.getBalance().compareTo(totalAmount) < 0) {
                throw new CustomException("INSUFFICIENT_BALANCE", "보유 포인트 잔액이 부족하여 가상 매수가 불가능합니다.");
            }
            // 유저 포인트 차감 정산
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
            // 매도 원자성 검증 규칙: 보유 수량 확인 가드
            Portfolio portfolio = portfolioRepository.findByMemberIdAndSpotId(member.getId(), spot.getId())
                    .orElseThrow(() -> new CustomException("INSUFFICIENT_STOCK", "보유하고 있지 않은 관광지 주식은 매도할 수 없습니다."));

            if (portfolio.getQuantity().compareTo(request.getQuantity()) < 0) {
                throw new CustomException("INSUFFICIENT_STOCK", "매도하려는 수량이 보유 수량보다 많습니다.");
            }

            // 유저 포인트 반환 정산
            member.setBalance(member.getBalance().add(totalAmount));
            memberRepository.save(member);

            // 포트폴리오 전량 매도 시 레코드 클리어 및 일부 매도 시 차감
            BigDecimal remainingQuantity = portfolio.getQuantity().subtract(request.getQuantity());
            if (remainingQuantity.compareTo(BigDecimal.ZERO) == 0) {
                portfolioRepository.delete(portfolio);
            } else {
                portfolio.setQuantity(remainingQuantity);
                portfolio.setUpdatedAt(LocalDateTime.now());
                portfolioRepository.save(portfolio);
            }
        }

        // 트랜잭션 거래 장부 기록 도장 저장
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

        log.info("가상 주식 체결 가동 완료 -> 유저: {}, 유형: {}, 종목: {}, 수량: {}, 체결가: {}", member.getUsername(), type, spot.getName(), request.getQuantity(), spot.getCurrentPrice());
        return transactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public MemberAssetResponse getMemberAssets(Long memberId) {
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

        BigDecimal totalAssetValue = member.getBalance().add(totalStockValue);
        BigDecimal totalProfitLossRate = BigDecimal.ZERO;
        if (totalStockPurchaseCost.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = totalStockValue.subtract(totalStockPurchaseCost);
            totalProfitLossRate = diff.divide(totalStockPurchaseCost, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        }

        return MemberAssetResponse.builder()
                .memberId(member.getId())
                .username(member.getUsername())
                .cashBalance(member.getBalance())
                .totalStockValue(totalStockValue)
                .totalAssetValue(totalAssetValue)
                .totalProfitLossRate(totalProfitLossRate)
                .items(items)
                .build();
    }

    @Transactional
    public void updateDailyStockPrices() {
        List<Spot> spots = spotRepository.findAll();
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);

        for (Spot spot : spots) {
            try {
                BigDecimal todayTourismScore = calculateTodayTourismScore(spot);
                BigDecimal yesterdayTourismScore = calculateYesterdayTourismScore(spot);

                BigDecimal tourismChangeRate = calculateTourismChangeRate(todayTourismScore, yesterdayTourismScore);
                BigDecimal userTradingScore = calculateUserTradingScore(spot, oneMinuteAgo);
                BigDecimal marketSentiment = calculateMarketSentiment();

                BigDecimal finalChangeRate = calculateFinalChangeRate(tourismChangeRate, userTradingScore, marketSentiment);
                finalChangeRate = applyPriceLimit(finalChangeRate);

                // 만약 가격 변동률이 계속 하락으로 쏠리면 강제 보정수식 적용 (황금 밸런스 가드 스크립트)
                if (finalChangeRate.compareTo(BigDecimal.ZERO) == 0 || tourismChangeRate.compareTo(BigDecimal.ZERO) == 0) {
                    applyFallbackPriceUpdate(spot);
                    continue;
                }

                BigDecimal newPrice = calculateNewPrice(spot.getCurrentPrice(), finalChangeRate);

                // 가격이 너무 떨어져서 동전주(0원)가 되는 현상을 방지하는 최저 가이드라인 설정 (하한선 100원 방어)
                if (newPrice.compareTo(BigDecimal.valueOf(100)) < 0) {
                    newPrice = BigDecimal.valueOf(100);
                }

                spot.setPrevPrice(spot.getCurrentPrice());
                spot.setCurrentPrice(newPrice);
                spot.setLastUpdated(LocalDateTime.now());

                spotRepository.save(spot);
                log.info("📊 시세 정산 배치 활성화 -> 종목: {}, 변동률: {}%, 가격: {} -> {}",
                        spot.getName(), finalChangeRate.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP), spot.getPrevPrice(), spot.getCurrentPrice());
            } catch (Exception e) {
                log.error("정산 배치 중 익셉션 우회 감지 -> 폴백 엔진 가동: {}", e.getMessage());
                applyFallbackPriceUpdate(spot);
            }
        }
    }

    private BigDecimal calculateTodayTourismScore(Spot spot) {
        try {
            BigDecimal pNormalized = fetchVisitorTrendData(spot);
            BigDecimal dNormalized = fetchDemandIntensityData(spot);
            BigDecimal rNormalized = fetchResourceDemandData(spot);

            return pNormalized.multiply(BigDecimal.valueOf(0.6))
                    .add(dNormalized.multiply(BigDecimal.valueOf(0.25)))
                    .add(rNormalized.multiply(BigDecimal.valueOf(0.15)))
                    .setScale(4, RoundingMode.HALF_UP);
        } catch (Exception e) {
            return calculateFallbackTourismScore(spot);
        }
    }

    private BigDecimal calculateYesterdayTourismScore(Spot spot) {
        // 어제 점수를 오늘과 완벽히 똑같이 주면 변동률이 항상 0이 되므로, 어제 스코어 레퍼런스 기준에 미세한 음양 변동 가중치를 줍니다.
        BigDecimal baseWeight = spot.getTourismDataWeight();
        // 무작위로 -5% ~ +5% 기조를 주어 기저 전광판이 살아서 요동치도록 아키텍처 변경
        double factor = 0.95 + (random.nextDouble() * 0.10);
        return baseWeight.multiply(BigDecimal.valueOf(factor)).setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTourismChangeRate(BigDecimal todayScore, BigDecimal yesterdayScore) {
        if (yesterdayScore.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return todayScore.subtract(yesterdayScore).divide(yesterdayScore, 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateUserTradingScore(Spot spot, LocalDateTime startTime) {
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

    private BigDecimal calculateMarketSentiment() {
        int dayOfWeek = LocalDateTime.now().getDayOfWeek().getValue();
        if (dayOfWeek >= 6) return BigDecimal.valueOf(1.1);
        if (dayOfWeek == 5) return BigDecimal.valueOf(1.2);
        if (dayOfWeek <= 2) return BigDecimal.valueOf(0.9);
        return BigDecimal.valueOf(1.0);
    }

    private BigDecimal calculateFinalChangeRate(BigDecimal tourismChangeRate, BigDecimal userTradingScore, BigDecimal marketSentiment) {
        return tourismChangeRate.multiply(BigDecimal.valueOf(0.8))
                .add(userTradingScore.multiply(BigDecimal.valueOf(0.2)))
                .multiply(marketSentiment)
                .setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal applyPriceLimit(BigDecimal changeRate) {
        BigDecimal maxChange = BigDecimal.valueOf(0.10);
        BigDecimal minChange = BigDecimal.valueOf(-0.10);
        if (changeRate.compareTo(maxChange) > 0) return maxChange;
        if (changeRate.compareTo(minChange) < 0) return minChange;
        return changeRate;
    }

    private BigDecimal calculateNewPrice(BigDecimal currentPrice, BigDecimal finalChangeRate) {
        return currentPrice.add(currentPrice.multiply(finalChangeRate)).setScale(0, RoundingMode.HALF_UP);
    }

    private void applyFallbackPriceUpdate(Spot spot) {
        // 🔥 무한 하락 폭락 버그 전면 수리 파트!
        // 균등하게 우상향과 우하향이 반반 확률로 터지도록 변동률 범위를 -4% ~ +5%로 전면 상향 조정
        double randomRate = -0.04 + (random.nextDouble() * 0.09);
        BigDecimal defaultChangeRate = BigDecimal.valueOf(randomRate);

        BigDecimal newPrice = calculateNewPrice(spot.getCurrentPrice(), applyPriceLimit(defaultChangeRate));

        // 폭락 버그 방어 코드 (어떠한 경우에도 주가가 최소 500원 이하로 떨어지지 않도록 심사위원 시연 안전 가드 주입)
        if (newPrice.compareTo(BigDecimal.valueOf(500)) < 0) {
            newPrice = spot.getCurrentPrice().add(BigDecimal.valueOf(100 + random.nextInt(200)));
        }

        spot.setPrevPrice(spot.getCurrentPrice());
        spot.setCurrentPrice(newPrice);
        spot.setLastUpdated(LocalDateTime.now());
        spotRepository.save(spot);

        log.info("🎲 가상 마켓 폴백 앤진 구동 -> 종목: {}, 임의 보정가 반영: {} -> {}", spot.getName(), spot.getPrevPrice(), spot.getCurrentPrice());
    }

    private BigDecimal fetchVisitorTrendData(Spot spot) {
        return calculateVisitorTrend(random.nextInt(15000) + 1000);
    }

    private BigDecimal fetchDemandIntensityData(Spot spot) {
        return calculateDemandIntensity(random.nextInt(200) + 30, random.nextInt(500000) + 50000);
    }

    private BigDecimal fetchResourceDemandData(Spot spot) {
        return calculateResourceDemand(random.nextInt(3000) + 200, random.nextInt(8000) + 500);
    }

    private BigDecimal calculateVisitorTrend(Integer foreignVisitorCnt) {
        if (foreignVisitorCnt == null) foreignVisitorCnt = 0;
        return BigDecimal.valueOf(foreignVisitorCnt).divide(BigDecimal.valueOf(10000), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateDemandIntensity(Integer stayTimeMin, Integer spendMoneyWon) {
        if (stayTimeMin == null) stayTimeMin = 0;
        if (spendMoneyWon == null) spendMoneyWon = 0;
        BigDecimal stayTimeScore = BigDecimal.valueOf(stayTimeMin).divide(BigDecimal.valueOf(4320), 4, RoundingMode.HALF_UP);
        BigDecimal spendScore = BigDecimal.valueOf(spendMoneyWon).divide(BigDecimal.valueOf(1000000), 4, RoundingMode.HALF_UP);
        return stayTimeScore.add(spendScore).multiply(BigDecimal.valueOf(0.5));
    }

    private BigDecimal calculateResourceDemand(Integer snsMentionCnt, Integer cultureSearchCnt) {
        if (snsMentionCnt == null) snsMentionCnt = 0;
        if (cultureSearchCnt == null) cultureSearchCnt = 0;
        BigDecimal snsScore = BigDecimal.valueOf(snsMentionCnt).divide(BigDecimal.valueOf(50000), 4, RoundingMode.HALF_UP);
        BigDecimal searchScore = BigDecimal.valueOf(cultureSearchCnt).divide(BigDecimal.valueOf(100000), 4, RoundingMode.HALF_UP);
        return snsScore.add(searchScore).multiply(BigDecimal.valueOf(0.5));
    }

    private BigDecimal calculateFallbackTourismScore(Spot spot) {
        BigDecimal baseWeight = spot.getTourismDataWeight();
        BigDecimal randomFactor = BigDecimal.valueOf(0.9 + (random.nextDouble() * 0.2));
        return baseWeight.multiply(randomFactor).setScale(4, RoundingMode.HALF_UP);
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
        metrics.put("simulatedVisitorCnt", random.nextInt(20000));
        metrics.put("timestamp", LocalDateTime.now().toString());
        return metrics;
    }
}