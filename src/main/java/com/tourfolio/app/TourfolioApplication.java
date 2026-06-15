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
        log.info("Initializing Tourfolio application with master data...");

        if (spotRepository.count() == 0) {
            initializeMasterData();
        } else {
            log.info("Master data already exists. Skipping initialization.");
        }
    }

    private void initializeMasterData() {
        log.info("Inserting master data for tourist spots...");

        Spot busanHaeundaeLCT = Spot.builder()
                .name("부산 해운대 엘시티")
                .areaCode("6")
                .contentId("2733967")
                .tier(1)
                .currentPrice(new BigDecimal("15000"))
                .prevPrice(new BigDecimal("15000"))
                .tourismDataWeight(new BigDecimal("0.05"))
                .lastUpdated(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        Spot seoulGyeongbokgung = Spot.builder()
                .name("서울 경복궁")
                .areaCode("1")
                .contentId("126508")
                .tier(2)
                .currentPrice(new BigDecimal("12000"))
                .prevPrice(new BigDecimal("12000"))
                .tourismDataWeight(new BigDecimal("0.04"))
                .lastUpdated(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        Spot gangneungAnmok = Spot.builder()
                .name("강릉 안목해변")
                .areaCode("32")
                .contentId("128470")
                .tier(3)
                .currentPrice(new BigDecimal("8000"))
                .prevPrice(new BigDecimal("8000"))
                .tourismDataWeight(new BigDecimal("0.03"))
                .lastUpdated(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        spotRepository.save(busanHaeundaeLCT);
        spotRepository.save(seoulGyeongbokgung);
        spotRepository.save(gangneungAnmok);

        log.info("Master data initialization completed. 3 spots inserted.");
    }
}
