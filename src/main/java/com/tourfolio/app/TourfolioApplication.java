package com.tourfolio.app;

import com.tourfolio.app.entity.Spot;
import com.tourfolio.app.repository.SpotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@SpringBootApplication
@EnableScheduling
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
        // 10개의 데이터를 DB 필수값에 맞춰서 넣습니다.
        List<Spot> spots = List.of(
                createSpot("경복궁", "11", "11110", "126508", 1, "10000", "서울", "서울 종로구", "url1", "경복궁설명", "문화", "역사", "10000", "10000"),
                createSpot("성산일출봉", "50", "50130", "126435", 1, "10000", "제주", "제주 서귀포", "url2", "일출봉설명", "자연", "바다", "10000", "10000"),
                createSpot("전주한옥마을", "52", "52111", "130456", 2, "5000", "전북", "전주 완산구", "url3", "한옥설명", "문화", "체험", "5000", "5000"),
                createSpot("남산서울타워", "11", "11140", "126487", 1, "10000", "서울", "서울 용산구", "url4", "남산설명", "전망", "야경", "10000", "10000"),
                createSpot("지리산 천왕봉", "48", "48240", "126543", 1, "10000", "경남", "경남 산청", "url5", "지리산설명", "자연", "등산", "10000", "10000"),
                createSpot("순천만국가정원", "46", "46150", "678901", 3, "2000", "전남", "전남 순천", "url6", "정원설명", "자연", "힐링", "2000", "2000"),
                createSpot("통영케이블카", "48", "48220", "567890", 3, "2000", "경남", "경남 통영", "url7", "케이블카설명", "체험", "바다", "2000", "2000"),
                createSpot("해운대해수욕장", "26", "26350", "126078", 1, "10000", "부산", "부산 해운대", "url8", "해운대설명", "자연", "바다", "10000", "10000"),
                createSpot("광안리해수욕장", "26", "26500", "126078", 2, "5000", "부산", "부산 수영구", "url9", "광안리설명", "자연", "야경", "5000", "5000"),
                createSpot("경주 불국사", "47", "47130", "126512", 2, "5000", "경북", "경북 경주", "url10", "불국사설명", "문화", "역사", "5000", "5000")
        );
        spotRepository.saveAll(spots);
        log.info("✅ 마스터 데이터 삽입 완료!");
    }

    private Spot createSpot(String name, String areaCode, String signguCd, String contentId, int tier, String basePrice,
                            String region, String address, String imageUrl, String desc, String theme, String tag, String initial, String ipo) {
        BigDecimal price = new BigDecimal(basePrice);
        return Spot.builder()
                .name(name).areaCode(areaCode).signguCd(signguCd).contentId(contentId).tier(tier)
                .region(region).address(address).imageUrl(imageUrl).description(desc)
                .theme(theme).themeTag(tag)
                .currentPrice(price).prevPrice(price)
                .initialPrice(new BigDecimal(initial)).ipoPrice(new BigDecimal(ipo))
                .tourismDataWeight(BigDecimal.ZERO).lastUpdated(LocalDateTime.now()).createdAt(LocalDateTime.now())
                .build();
    }
}