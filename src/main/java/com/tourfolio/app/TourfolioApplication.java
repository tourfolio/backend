package com.tourfolio.app;

import com.tourfolio.app.entity.Spot;
import com.tourfolio.app.repository.SpotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class TourfolioApplication implements CommandLineRunner {

    private final SpotRepository spotRepository;

    public static void main(String[] args) {
        SpringApplication.run(TourfolioApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (spotRepository.count() == 0) {
            log.info("🚀 마스터 데이터 초기화 시작...");
            initializeMasterData();
        }
    }

    private void initializeMasterData() {
        List<Spot> spots = List.of(
                createSpot("경복궁", "11", "11110", "126508", 1, "10000", "서울", "역사"),
                createSpot("성산일출봉", "50", "50130", "126435", 1, "10000", "제주", "자연"),
                createSpot("전주한옥마을", "52", "52111", "130456", 2, "5000", "전북", "문화"),
                createSpot("남산서울타워", "11", "11140", "126487", 1, "10000", "서울", "레저"),
                createSpot("지리산 천왕봉", "48", "48240", "126543", 1, "10000", "경남", "자연"),
                createSpot("순천만국가정원", "46", "46150", "678901", 3, "2000", "전남", "자연"),
                createSpot("통영케이블카", "48", "48220", "567890", 3, "2000", "경남", "레저"),
                createSpot("해운대해수욕장", "26", "26350", "126078", 1, "10000", "부산", "레저"),
                createSpot("광안리해수욕장", "26", "26500", "126078", 2, "5000", "부산", "레저"),
                createSpot("경주 불국사", "47", "47130", "126512", 2, "5000", "경북", "역사")
        );
        spotRepository.saveAll(spots);
        log.info("✅ 마스터 데이터 삽입 완료!");
    }

    private Spot createSpot(String name, String areaCode, String signguCd, String contentId, int tier, String basePrice, String region, String theme) {
        BigDecimal initialPrice = new BigDecimal(basePrice);
        return Spot.builder()
                .name(name)
                .areaCode(areaCode)
                .signguCd(signguCd)
                .contentId(contentId)
                .tier(tier)
                .region(region)
                .theme(theme)
                .currentPrice(initialPrice)
                .prevPrice(initialPrice)
                .initialPrice(initialPrice)
                .ipoPrice(initialPrice)
                .tourismDataWeight(BigDecimal.ZERO)
                .lastUpdated(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
    }
}