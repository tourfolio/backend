package com.tourfolio.app.config;

import com.tourfolio.app.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class SchedulerConfig {

    private final StockService stockService;

    @Scheduled(fixedRate = 60000)
    public void updateDailyStockPrices() {
        log.info("⏰ 1분 주기 배치: 실시간 관광 지표 및 체결 시세 정산 롤링 시작");
        try {
            stockService.updateDailyStockPrices();
        } catch (Exception e) {
            log.error("정산 배치 도중 예외가 차단되었습니다: {}", e.getMessage());
        }
    }
}