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
        log.info("Initializing Tourfolio application with core master data...");

        if (spotRepository.count() == 0) {
            initializeMasterData();
        } else {
            log.info("Master data already exists. Skipping initialization.");
        }
    }

    private void initializeMasterData() {
        log.info("Inserting core metadata for API synchronization...");

        // 1티어 종목 (베이스 공모가: 10,000P)
        Spot busanHaeundaeLCT = createCoreMetadata("부산 해운대 엘시티", "6", "2733967", 1, "10000");
        Spot seoulGyeongbokgung = createCoreMetadata("서울 경복궁", "1", "126508", 1, "10000");
        Spot jejuSeongsan = createCoreMetadata("제주 성산일출봉", "39", "126435", 1, "10000");

        // 2티어 종목 (베이스 공모가: 5,000P)
        Spot busanGwanganri = createCoreMetadata("부산 광안리 해수욕장", "6", "126078", 2, "5000");
        Spot jeonjuHanok = createCoreMetadata("전주 한옥마을", "37", "130456", 2, "5000");
        Spot gyeongjuBulguksa = createCoreMetadata("경주 불국사", "35", "126512", 2, "5000");

        // 3티어 종목 (베이스 공모가: 2,000P)
        Spot tongyeongCable = createCoreMetadata("통영 케이블카", "36", "567890", 3, "2000");
        Spot suncheonBay = createCoreMetadata("순천만 국가정원", "38", "678901", 3, "2000");

        // 4티어 종목 (베이스 공모가: 1,000P)
        Spot dokdo = createCoreMetadata("독도", "32", "999999", 4, "1000");
        Spot hallasan = createCoreMetadata("한라산 정상", "39", "888888", 4, "1000");

        // 10개 핵심 메타데이터만 깔끔하게 저장
        spotRepository.saveAll(List.of(
                busanHaeundaeLCT, seoulGyeongbokgung, jejuSeongsan,
                busanGwanganri, jeonjuHanok, gyeongjuBulguksa,
                tongyeongCable, suncheonBay,
                dokdo, hallasan
        ));

        log.info("Core master data initialized successfully. Ready for 9 AM API Scheduler.");
    }

    /**
     * 불러올 데이터(관광지명, 지역코드, 콘텐츠 ID) 중심의 빌더 메소드
     */
    private Spot createCoreMetadata(String name, String areaCode, String contentId, int tier, String basePrice) {
        BigDecimal initialPrice = new BigDecimal(basePrice);

        return Spot.builder()
                .name(name)
                .areaCode(areaCode)
                .contentId(contentId)
                .tier(tier)
                .initialPrice(initialPrice)
                .currentPrice(initialPrice)
                .prevPrice(initialPrice)
                .tourismDataWeight(BigDecimal.ZERO) // 스케줄러가 돌기 전까지는 0으로 초기화
                .address("")
                .areaName("") // 오전 9시 API 배치 처리 시 업데이트될 영역
                .signguCd("") // 오전 9시 API 배치 처리 시 업데이트될 영역
                .description(name + " 메타데이터 종목")
                .imageUrl("")
                .region("")
                .theme("")
                .themeTag("")
                .lastUpdated(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
    }
}