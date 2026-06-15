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
        log.info("Starting scheduled daily stock price update...");
        try {
            stockService.updateDailyStockPrices();
            log.info("Scheduled daily stock price update completed successfully.");
        } catch (Exception e) {
            log.error("Error during scheduled stock price update: {}", e.getMessage(), e);
        }
    }
}
