package com.tourfolio.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourfolio.app.dto.OpenApiDto;
import com.tourfolio.app.dto.StockResponse;
import com.tourfolio.app.dto.TradeRequest;
import com.tourfolio.app.entity.Spot;
import com.tourfolio.app.entity.Transaction;
import com.tourfolio.app.repository.SpotRepository;
import com.tourfolio.app.repository.TransactionRepository;
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
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    @Value("${openapi.service.key}")
    private String openApiServiceKey;

    // 관광 수요 강도 지표 API 엔드포인트
    @Value("${endpoint.demand}")
    private String endpointDemand;

    // 관광 자원 수요 지표 API 엔드포인트
    @Value("${endpoint.resdem}")
    private String endpointResdem;

    // 방문자 추이 지표 API 엔드포인트
    @Value("${endpoint.visitor}")
    private String endpointVisitor;

    // 집중률 예측 지표 API 엔드포인트
    @Value("${endpoint.forecast}")
    private String endpointForecast;

    @Transactional
    public Transaction executeTrade(TradeRequest request) {
        Spot spot = spotRepository.findById(request.getSpotId())
                .orElseThrow(() -> new IllegalArgumentException("Spot not found with id: " + request.getSpotId()));

        if (!"BUY".equalsIgnoreCase(request.getType()) && !"SELL".equalsIgnoreCase(request.getType())) {
            throw new IllegalArgumentException("Invalid trade type. Must be BUY or SELL.");
        }

        if (request.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }

        BigDecimal totalAmount = spot.getCurrentPrice().multiply(request.getQuantity())
                .setScale(2, RoundingMode.HALF_UP);

        Transaction transaction = Transaction.builder()
                .spotId(spot.getId())
                .type(request.getType().toUpperCase())
                .quantity(request.getQuantity())
                .price(spot.getCurrentPrice())
                .totalAmount(totalAmount)
                .executedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Trade executed: {} {} of {} at {}", request.getType(), request.getQuantity(), spot.getName(), spot.getCurrentPrice());

        return savedTransaction;
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
                
                BigDecimal newPrice = calculateNewPrice(spot.getCurrentPrice(), finalChangeRate);
                
                spot.setPrevPrice(spot.getCurrentPrice());
                spot.setCurrentPrice(newPrice);
                spot.setLastUpdated(LocalDateTime.now());
                
                spotRepository.save(spot);
                
                log.info("Updated price for {}: {} -> {} (Tourism: {}, User: {}, Market: {})", 
                    spot.getName(), spot.getPrevPrice(), spot.getCurrentPrice(), 
                    tourismChangeRate, userTradingScore, marketSentiment);
            } catch (Exception e) {
                log.error("Error updating price for {}: {}", spot.getName(), e.getMessage(), e);
                applyFallbackPriceUpdate(spot);
            }
        }
    }

    private BigDecimal calculateTodayTourismScore(Spot spot) {
        try {
            BigDecimal pNormalized = fetchVisitorTrendData(spot);
            BigDecimal dNormalized = fetchDemandIntensityData(spot);
            BigDecimal rNormalized = fetchResourceDemandData(spot);
            
            BigDecimal tourismScore = pNormalized.multiply(BigDecimal.valueOf(0.6))
                    .add(dNormalized.multiply(BigDecimal.valueOf(0.25)))
                    .add(rNormalized.multiply(BigDecimal.valueOf(0.15)));
            
            return tourismScore.setScale(4, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.error("Error calculating today tourism score for {}: {}", spot.getName(), e.getMessage());
            return calculateFallbackTourismScore(spot);
        }
    }

    private BigDecimal calculateYesterdayTourismScore(Spot spot) {
        try {
            BigDecimal pNormalized = fetchVisitorTrendData(spot);
            BigDecimal dNormalized = fetchDemandIntensityData(spot);
            BigDecimal rNormalized = fetchResourceDemandData(spot);
            
            BigDecimal tourismScore = pNormalized.multiply(BigDecimal.valueOf(0.6))
                    .add(dNormalized.multiply(BigDecimal.valueOf(0.25)))
                    .add(rNormalized.multiply(BigDecimal.valueOf(0.15)));
            
            return tourismScore.setScale(4, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.error("Error calculating yesterday tourism score for {}: {}", spot.getName(), e.getMessage());
            return calculateFallbackTourismScore(spot);
        }
    }

    private BigDecimal calculateTourismChangeRate(BigDecimal todayScore, BigDecimal yesterdayScore) {
        if (yesterdayScore.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal change = todayScore.subtract(yesterdayScore);
        return change.divide(yesterdayScore, 4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateUserTradingScore(Spot spot, LocalDateTime startTime) {
        List<Transaction> recentTransactions = transactionRepository.findBySpotIdAndCreatedAtAfterOrderByCreatedAtAsc(spot.getId(), startTime);
        
        if (recentTransactions.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal buyVolume = BigDecimal.ZERO;
        BigDecimal sellVolume = BigDecimal.ZERO;
        
        for (Transaction tx : recentTransactions) {
            if ("BUY".equals(tx.getType())) {
                buyVolume = buyVolume.add(tx.getQuantity());
            } else if ("SELL".equals(tx.getType())) {
                sellVolume = sellVolume.add(tx.getQuantity());
            }
        }
        
        BigDecimal totalVolume = buyVolume.add(sellVolume);
        if (totalVolume.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal netBuyVolume = buyVolume.subtract(sellVolume);
        BigDecimal userTradingScore = netBuyVolume.divide(totalVolume, 4, RoundingMode.HALF_UP);
        
        return userTradingScore;
    }

    private BigDecimal calculateMarketSentiment() {
        int dayOfWeek = LocalDateTime.now().getDayOfWeek().getValue();
        
        if (dayOfWeek == 6 || dayOfWeek == 7) {
            return BigDecimal.valueOf(1.1);
        } else if (dayOfWeek == 5) {
            return BigDecimal.valueOf(1.2);
        } else if (dayOfWeek == 1 || dayOfWeek == 2) {
            return BigDecimal.valueOf(0.9);
        } else {
            return BigDecimal.valueOf(1.0);
        }
    }

    private BigDecimal calculateFinalChangeRate(BigDecimal tourismChangeRate, BigDecimal userTradingScore, BigDecimal marketSentiment) {
        BigDecimal tourismWeighted = tourismChangeRate.multiply(BigDecimal.valueOf(0.8));
        BigDecimal userWeighted = userTradingScore.multiply(BigDecimal.valueOf(0.2));
        
        BigDecimal baseChangeRate = tourismWeighted.add(userWeighted);
        BigDecimal finalChangeRate = baseChangeRate.multiply(marketSentiment);
        
        return finalChangeRate.setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal applyPriceLimit(BigDecimal changeRate) {
        BigDecimal maxChange = BigDecimal.valueOf(0.10);
        BigDecimal minChange = BigDecimal.valueOf(-0.10);
        
        if (changeRate.compareTo(maxChange) > 0) {
            return maxChange;
        }
        if (changeRate.compareTo(minChange) < 0) {
            return minChange;
        }
        
        return changeRate;
    }

    private BigDecimal calculateNewPrice(BigDecimal currentPrice, BigDecimal finalChangeRate) {
        BigDecimal priceChange = currentPrice.multiply(finalChangeRate);
        BigDecimal newPrice = currentPrice.add(priceChange);
        
        return newPrice.setScale(0, RoundingMode.HALF_UP);
    }

    private void applyFallbackPriceUpdate(Spot spot) {
        BigDecimal defaultChangeRate = BigDecimal.valueOf(0.01 + (random.nextDouble() * 0.02 - 0.01));
        defaultChangeRate = applyPriceLimit(defaultChangeRate);
        
        BigDecimal newPrice = calculateNewPrice(spot.getCurrentPrice(), defaultChangeRate);
        
        spot.setPrevPrice(spot.getCurrentPrice());
        spot.setCurrentPrice(newPrice);
        spot.setLastUpdated(LocalDateTime.now());
        
        spotRepository.save(spot);
        
        log.info("Applied fallback price update for {}: {} -> {}", spot.getName(), spot.getPrevPrice(), spot.getCurrentPrice());
    }

    private BigDecimal fetchVisitorTrendData(Spot spot) {
        try {
            URI apiUrl = UriComponentsBuilder.fromHttpUrl(endpointVisitor)
                    .path("/metabolicVisitorCnt")
                    .queryParam("serviceKey", openApiServiceKey)
                    .queryParam("contentId", spot.getContentId())
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "Tourfolio")
                    .queryParam("_type", "json")
                    .build(true)
                    .toUri();
            
            String response = restTemplate.getForObject(apiUrl, String.class);
            
            if (response != null && !response.isEmpty()) {
                return calculateVisitorTrendFromResponse(response);
            }
        } catch (Exception e) {
            log.error("Error fetching visitor trend data for {}: {}", spot.getName(), e.getMessage());
        }
        
        return calculateVisitorTrend(null);
    }

    private BigDecimal fetchDemandIntensityData(Spot spot) {
        try {
            URI apiUrl = UriComponentsBuilder.fromHttpUrl(endpointDemand)
                    .path("/AreaBasedTourDemDsEst")
                    .queryParam("serviceKey", openApiServiceKey)
                    .queryParam("contentId", spot.getContentId())
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "Tourfolio")
                    .queryParam("_type", "json")
                    .build(true)
                    .toUri();
            
            String response = restTemplate.getForObject(apiUrl, String.class);
            
            if (response != null && !response.isEmpty()) {
                return calculateDemandIntensityFromResponse(response);
            }
        } catch (Exception e) {
            log.error("Error fetching demand intensity data for {}: {}", spot.getName(), e.getMessage());
        }
        
        return calculateDemandIntensity(null, null);
    }

    private BigDecimal fetchResourceDemandData(Spot spot) {
        try {
            URI apiUrl = UriComponentsBuilder.fromHttpUrl(endpointResdem)
                    .path("/areaBasedResourceDemandEst")
                    .queryParam("serviceKey", openApiServiceKey)
                    .queryParam("contentId", spot.getContentId())
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "Tourfolio")
                    .queryParam("_type", "json")
                    .build(true)
                    .toUri();
            
            String response = restTemplate.getForObject(apiUrl, String.class);
            
            if (response != null && !response.isEmpty()) {
                return calculateResourceDemandFromResponse(response);
            }
        } catch (Exception e) {
            log.error("Error fetching resource demand data for {}: {}", spot.getName(), e.getMessage());
        }
        
        return calculateResourceDemand(null, null);
    }

    private BigDecimal calculateVisitorTrendFromResponse(String response) {
        try {
            OpenApiDto.VisitorResponse visitorResponse = objectMapper.readValue(response, OpenApiDto.VisitorResponse.class);
            if (visitorResponse.getResponse() != null && 
                visitorResponse.getResponse().getBody() != null &&
                visitorResponse.getResponse().getBody().getItems() != null &&
                visitorResponse.getResponse().getBody().getItems().getItem() != null &&
                !visitorResponse.getResponse().getBody().getItems().getItem().isEmpty()) {
                OpenApiDto.VisitorResponse.VisitorItem item = visitorResponse.getResponse().getBody().getItems().getItem().get(0);
                return calculateVisitorTrend(item.getForeignVisitorCnt());
            }
        } catch (Exception e) {
            log.error("Error parsing visitor trend response: {}", e.getMessage());
        }
        return calculateVisitorTrend(null);
    }

    private BigDecimal calculateDemandIntensityFromResponse(String response) {
        try {
            OpenApiDto.DemandResponse demandResponse = objectMapper.readValue(response, OpenApiDto.DemandResponse.class);
            if (demandResponse.getResponse() != null && 
                demandResponse.getResponse().getBody() != null &&
                demandResponse.getResponse().getBody().getItems() != null &&
                demandResponse.getResponse().getBody().getItems().getItem() != null &&
                !demandResponse.getResponse().getBody().getItems().getItem().isEmpty()) {
                OpenApiDto.DemandResponse.DemandItem item = demandResponse.getResponse().getBody().getItems().getItem().get(0);
                return calculateDemandIntensity(item.getStayTimeMin(), item.getSpendMoneyWon());
            }
        } catch (Exception e) {
            log.error("Error parsing demand intensity response: {}", e.getMessage());
        }
        return calculateDemandIntensity(null, null);
    }

    private BigDecimal calculateResourceDemandFromResponse(String response) {
        try {
            OpenApiDto.ResourceResponse resourceResponse = objectMapper.readValue(response, OpenApiDto.ResourceResponse.class);
            if (resourceResponse.getResponse() != null && 
                resourceResponse.getResponse().getBody() != null &&
                resourceResponse.getResponse().getBody().getItems() != null &&
                resourceResponse.getResponse().getBody().getItems().getItem() != null &&
                !resourceResponse.getResponse().getBody().getItems().getItem().isEmpty()) {
                OpenApiDto.ResourceResponse.ResourceItem item = resourceResponse.getResponse().getBody().getItems().getItem().get(0);
                return calculateResourceDemand(item.getSnsMentionCnt(), item.getCultureSearchCnt());
            }
        } catch (Exception e) {
            log.error("Error parsing resource demand response: {}", e.getMessage());
        }
        return calculateResourceDemand(null, null);
    }

    private BigDecimal calculateVisitorTrend(Integer foreignVisitorCnt) {
        if (foreignVisitorCnt == null) {
            foreignVisitorCnt = 0;
        }
        
        BigDecimal baseRate = BigDecimal.valueOf(foreignVisitorCnt).divide(BigDecimal.valueOf(10000), 4, RoundingMode.HALF_UP);
        BigDecimal randomFactor = BigDecimal.valueOf(0.8 + (random.nextDouble() * 0.4));
        
        return baseRate.multiply(randomFactor).setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateDemandIntensity(Integer stayTimeMin, Integer spendMoneyWon) {
        if (stayTimeMin == null) {
            stayTimeMin = 0;
        }
        if (spendMoneyWon == null) {
            spendMoneyWon = 0;
        }
        
        BigDecimal stayTimeScore = BigDecimal.valueOf(stayTimeMin).divide(BigDecimal.valueOf(4320), 4, RoundingMode.HALF_UP);
        BigDecimal spendScore = BigDecimal.valueOf(spendMoneyWon).divide(BigDecimal.valueOf(1000000), 4, RoundingMode.HALF_UP);
        
        BigDecimal intensity = stayTimeScore.multiply(BigDecimal.valueOf(0.5)).add(spendScore.multiply(BigDecimal.valueOf(0.5)));
        BigDecimal randomFactor = BigDecimal.valueOf(0.7 + (random.nextDouble() * 0.6));
        
        return intensity.multiply(randomFactor).setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateResourceDemand(Integer snsMentionCnt, Integer cultureSearchCnt) {
        if (snsMentionCnt == null) {
            snsMentionCnt = 0;
        }
        if (cultureSearchCnt == null) {
            cultureSearchCnt = 0;
        }
        
        BigDecimal snsScore = BigDecimal.valueOf(snsMentionCnt).divide(BigDecimal.valueOf(50000), 4, RoundingMode.HALF_UP);
        BigDecimal searchScore = BigDecimal.valueOf(cultureSearchCnt).divide(BigDecimal.valueOf(100000), 4, RoundingMode.HALF_UP);
        
        BigDecimal resourceDemand = snsScore.multiply(BigDecimal.valueOf(0.5)).add(searchScore.multiply(BigDecimal.valueOf(0.5)));
        BigDecimal randomFactor = BigDecimal.valueOf(0.65 + (random.nextDouble() * 0.7));
        
        return resourceDemand.multiply(randomFactor).setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateFallbackTourismScore(Spot spot) {
        BigDecimal baseWeight = spot.getTourismDataWeight();
        BigDecimal randomFactor = BigDecimal.valueOf(0.9 + (random.nextDouble() * 0.2));
        
        return baseWeight.multiply(randomFactor).setScale(4, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTradingVolumeWeight(Spot spot, LocalDateTime startTime) {
        BigDecimal netBuyVolume = transactionRepository.calculateNetBuyVolume(spot.getId(), startTime);
        
        if (netBuyVolume == null) {
            netBuyVolume = BigDecimal.ZERO;
        }

        BigDecimal volumeFactor = netBuyVolume.divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP);
        volumeFactor = volumeFactor.multiply(BigDecimal.valueOf(0.01));
        
        if (volumeFactor.compareTo(BigDecimal.valueOf(0.2)) > 0) {
            volumeFactor = BigDecimal.valueOf(0.2);
        }
        if (volumeFactor.compareTo(BigDecimal.valueOf(-0.2)) < 0) {
            volumeFactor = BigDecimal.valueOf(-0.2);
        }
        
        return volumeFactor;
    }

    public List<StockResponse> getAllStocks() {
        List<Spot> spots = spotRepository.findAllByOrderByTierAscNameAsc();
        
        return spots.stream()
                .map(this::mapToStockResponse)
                .toList();
    }

    private StockResponse mapToStockResponse(Spot spot) {
        BigDecimal changeRate = calculateChangeRate(spot.getCurrentPrice(), spot.getPrevPrice());
        
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

    private BigDecimal calculateChangeRate(BigDecimal currentPrice, BigDecimal prevPrice) {
        if (prevPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal change = currentPrice.subtract(prevPrice);
        return change.divide(prevPrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public Map<String, Object> testOpenApiMetrics(Long spotId) {
        Spot spot = spotRepository.findById(spotId)
                .orElseThrow(() -> new IllegalArgumentException("Spot not found with id: " + spotId));

        Map<String, Object> metrics = new HashMap<>();
        
        Integer stayTimeMinutes = random.nextInt(4321);
        BigDecimal stayTimePercentage = BigDecimal.valueOf(stayTimeMinutes)
                .divide(BigDecimal.valueOf(4320), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        
        Map<String, Object> stayTimeData = new HashMap<>();
        stayTimeData.put("originalValue", stayTimeMinutes);
        stayTimeData.put("unit", "minutes");
        stayTimeData.put("maxReference", 4320);
        stayTimeData.put("percentage", stayTimePercentage);
        metrics.put("stayTime", stayTimeData);
        
        Integer cardConsumptionAmount = random.nextInt(1000000) + 10000;
        double logScale = Math.log10(cardConsumptionAmount);
        BigDecimal cardConsumptionPercentage = BigDecimal.valueOf(logScale)
                .divide(BigDecimal.valueOf(6), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        
        Map<String, Object> cardConsumptionData = new HashMap<>();
        cardConsumptionData.put("originalValue", cardConsumptionAmount);
        cardConsumptionData.put("unit", "KRW");
        cardConsumptionData.put("logScale", logScale);
        cardConsumptionData.put("percentage", cardConsumptionPercentage);
        metrics.put("cardConsumption", cardConsumptionData);
        
        Integer snsMentions = random.nextInt(50000) + 100;
        BigDecimal snsMentionsPercentage = BigDecimal.valueOf(snsMentions)
                .divide(BigDecimal.valueOf(50000), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        
        Map<String, Object> snsMentionsData = new HashMap<>();
        snsMentionsData.put("originalValue", snsMentions);
        snsMentionsData.put("unit", "count");
        snsMentionsData.put("maxReference", 50000);
        snsMentionsData.put("percentage", snsMentionsPercentage);
        metrics.put("snsMentions", snsMentionsData);
        
        Integer navigationSearchVolume = random.nextInt(100000) + 500;
        BigDecimal navigationSearchPercentage = BigDecimal.valueOf(navigationSearchVolume)
                .divide(BigDecimal.valueOf(100000), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        
        Map<String, Object> navigationSearchData = new HashMap<>();
        navigationSearchData.put("originalValue", navigationSearchVolume);
        navigationSearchData.put("unit", "count");
        navigationSearchData.put("maxReference", 100000);
        navigationSearchData.put("percentage", navigationSearchPercentage);
        metrics.put("navigationSearch", navigationSearchData);
        
        Map<String, Object> spotInfo = new HashMap<>();
        spotInfo.put("id", spot.getId());
        spotInfo.put("name", spot.getName());
        spotInfo.put("areaCode", spot.getAreaCode());
        spotInfo.put("contentId", spot.getContentId());
        metrics.put("spotInfo", spotInfo);
        
        metrics.put("timestamp", LocalDateTime.now().toString());
        
        log.info("Test API metrics generated for spot {}: {}", spot.getName(), metrics);
        
        return metrics;
    }
}
