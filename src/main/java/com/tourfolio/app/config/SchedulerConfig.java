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

    // 자동 주가 생성 비활성화 (가짜 데이터 생성 방지)
    // @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    // public void updateDailyStockPrices() {
    //     log.info("⏰ 매일 오전 9시 (한국 시간) 배치: 관광 지표 기반 주가 정산 시작");
    //     try {
    //         stockService.updateDailyStockPrices();
    //         log.info("✅ 주가 정산 배치 완료");
    //     } catch (Exception e) {
    //         log.error("❌ 정산 배치 도중 예외 발생: {}", e.getMessage(), e);
    //     }
    // }
}