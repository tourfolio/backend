package com.tourfolio.app.controller;

import com.tourfolio.app.api.client.AreaTarDemDsClient;
import com.tourfolio.app.api.client.AreaTarResDemClient;
import com.tourfolio.app.api.client.DataLabClient;
import com.tourfolio.app.api.client.TatsCnctrRateClient;
import com.tourfolio.app.api.dto.AreaTarDemDsDto;
import com.tourfolio.app.api.dto.AreaTarResDemDto;
import com.tourfolio.app.api.dto.DataLabDto;
import com.tourfolio.app.api.dto.TatsCnctrRateDto;
import com.tourfolio.app.dto.DebugResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/debug/indicators")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Debug Indicators", description = "공공데이터 원본 디버깅용 API")
public class DebugController {

    private final TatsCnctrRateClient tatsCnctrRateClient;
    private final AreaTarDemDsClient areaTarDemDsClient;
    private final AreaTarResDemClient areaTarResDemClient;
    private final DataLabClient dataLabClient;

    @GetMapping("/concentration")
    @Operation(summary = "집중률 예측(P) Raw 데이터 조회", description = "관광지 집중률 예측 데이터를 원본 형태로 조회합니다.")
    public ResponseEntity<DebugResponse<List<TatsCnctrRateDto>>> getConcentrationData(
            @Parameter(description = "지역 코드 (예: 1=서울, 6=제주)") @RequestParam(defaultValue = "1") String areaCd,
            @Parameter(description = "시군구 코드 (예: 11110=종로구)") @RequestParam(defaultValue = "11110") String signguCd,
            @Parameter(description = "관광지 이름") @RequestParam(defaultValue = "경복궁") String tAtsNm) {
        
        log.info("디버깅: 집중률 예측 데이터 조회 - areaCd={}, signguCd={}, tAtsNm={}", areaCd, signguCd, tAtsNm);
        
        try {
            List<TatsCnctrRateDto> data = tatsCnctrRateClient.fetchPredictions(areaCd, signguCd, tAtsNm);
            log.info("디버깅: 집중률 예측 데이터 조회 성공 - 데이터 수: {}", data.size());
            
            return ResponseEntity.ok(
                DebugResponse.<List<TatsCnctrRateDto>>builder()
                    .success(true)
                    .message("집중률 예측 데이터 조회 성공")
                    .data(data)
                    .metadata(Map.of(
                        "areaCd", areaCd,
                        "signguCd", signguCd,
                        "tAtsNm", tAtsNm,
                        "count", data.size()
                    ))
                    .build()
            );
        } catch (Exception e) {
            log.error("디버깅: 집중률 예측 데이터 조회 실패", e);
            return ResponseEntity.status(500).body(
                DebugResponse.<List<TatsCnctrRateDto>>builder()
                    .success(false)
                    .message("집중률 예측 데이터 조회 실패: " + e.getMessage())
                    .data(List.of())
                    .build()
            );
        }
    }

    @GetMapping("/demand-intensity")
    @Operation(summary = "수요 강도(D) Raw 데이터 조회", description = "지역별 관광 수요 강도(체류/소비) 데이터를 원본 형태로 조회합니다.")
    public ResponseEntity<DebugResponse<Map<String, List<AreaTarDemDsDto>>>> getDemandIntensityData(
            @Parameter(description = "지역 코드 (예: 1=서울, 6=제주)") @RequestParam(defaultValue = "1") String areaCd,
            @Parameter(description = "시군구 코드 (예: 11110=종로구)") @RequestParam(defaultValue = "11110") String signguCd) {
        
        log.info("디버깅: 수요 강도 데이터 조회 - areaCd={}, signguCd={}", areaCd, signguCd);
        
        try {
            List<AreaTarDemDsDto> stayIntensity = areaTarDemDsClient.fetchStayIntensity(areaCd, signguCd);
            List<AreaTarDemDsDto> spendIntensity = areaTarDemDsClient.fetchSpendIntensity(areaCd, signguCd);
            
            log.info("디버깅: 수요 강도 데이터 조회 성공 - 체류: {}, 소비: {}", stayIntensity.size(), spendIntensity.size());
            
            return ResponseEntity.ok(
                DebugResponse.<Map<String, List<AreaTarDemDsDto>>>builder()
                    .success(true)
                    .message("수요 강도 데이터 조회 성공")
                    .data(Map.of(
                        "stayIntensity", stayIntensity,
                        "spendIntensity", spendIntensity
                    ))
                    .metadata(Map.of(
                        "areaCd", areaCd,
                        "signguCd", signguCd,
                        "stayIntensityCount", stayIntensity.size(),
                        "spendIntensityCount", spendIntensity.size()
                    ))
                    .build()
            );
        } catch (Exception e) {
            log.error("디버깅: 수요 강도 데이터 조회 실패", e);
            return ResponseEntity.status(500).body(
                DebugResponse.<Map<String, List<AreaTarDemDsDto>>>builder()
                    .success(false)
                    .message("수요 강도 데이터 조회 실패: " + e.getMessage())
                    .data(Map.of())
                    .build()
            );
        }
    }

    @GetMapping("/resource-demand")
    @Operation(summary = "자원 수요(R) Raw 데이터 조회", description = "지역별 관광 자원 수요(서비스/문화) 데이터를 원본 형태로 조회합니다.")
    public ResponseEntity<DebugResponse<Map<String, List<AreaTarResDemDto>>>> getResourceDemandData(
            @Parameter(description = "지역 코드 (예: 1=서울, 6=제주)") @RequestParam(defaultValue = "1") String areaCd,
            @Parameter(description = "시군구 코드 (예: 11110=종로구)") @RequestParam(defaultValue = "11110") String signguCd) {
        
        log.info("디버깅: 자원 수요 데이터 조회 - areaCd={}, signguCd={}", areaCd, signguCd);
        
        try {
            List<AreaTarResDemDto> serviceDemand = areaTarResDemClient.fetchServiceDemand(areaCd, signguCd);
            List<AreaTarResDemDto> cultureDemand = areaTarResDemClient.fetchCultureDemand(areaCd, signguCd);
            
            log.info("디버깅: 자원 수요 데이터 조회 성공 - 서비스: {}, 문화: {}", serviceDemand.size(), cultureDemand.size());
            
            return ResponseEntity.ok(
                DebugResponse.<Map<String, List<AreaTarResDemDto>>>builder()
                    .success(true)
                    .message("자원 수요 데이터 조회 성공")
                    .data(Map.of(
                        "serviceDemand", serviceDemand,
                        "cultureDemand", cultureDemand
                    ))
                    .metadata(Map.of(
                        "areaCd", areaCd,
                        "signguCd", signguCd,
                        "serviceDemandCount", serviceDemand.size(),
                        "cultureDemandCount", cultureDemand.size()
                    ))
                    .build()
            );
        } catch (Exception e) {
            log.error("디버깅: 자원 수요 데이터 조회 실패", e);
            return ResponseEntity.status(500).body(
                DebugResponse.<Map<String, List<AreaTarResDemDto>>>builder()
                    .success(false)
                    .message("자원 수요 데이터 조회 실패: " + e.getMessage())
                    .data(Map.of())
                    .build()
            );
        }
    }

    @GetMapping("/visitor-count")
    @Operation(summary = "방문자수(S) Raw 데이터 조회", description = "광역 지자체 지역방문자수 빅데이터를 원본 형태로 조회합니다.")
    public ResponseEntity<DebugResponse<List<DataLabDto>>> getVisitorCountData(
            @Parameter(description = "지역 코드 (예: 1=서울, 6=제주)") @RequestParam(defaultValue = "1") String areaCd,
            @Parameter(description = "조회 시작일 (yyyyMMdd, 기본: 30일 전)") @RequestParam(required = false) String startDate,
            @Parameter(description = "조회 종료일 (yyyyMMdd, 기본: 오늘)") @RequestParam(required = false) String endDate) {
        
        LocalDate start = startDate != null ? LocalDate.parse(startDate, java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) 
                                            : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? LocalDate.parse(endDate, java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) 
                                        : LocalDate.now();
        
        log.info("디버깅: 방문자수 데이터 조회 - areaCd={}, startDate={}, endDate={}", areaCd, start, end);
        
        try {
            List<DataLabDto> data = dataLabClient.fetchVisitorCounts(areaCd, start, end);
            log.info("디버깅: 방문자수 데이터 조회 성공 - 데이터 수: {}", data.size());
            
            return ResponseEntity.ok(
                DebugResponse.<List<DataLabDto>>builder()
                    .success(true)
                    .message("방문자수 데이터 조회 성공")
                    .data(data)
                    .metadata(Map.of(
                        "areaCd", areaCd,
                        "startDate", start.toString(),
                        "endDate", end.toString(),
                        "count", data.size()
                    ))
                    .build()
            );
        } catch (Exception e) {
            log.error("디버깅: 방문자수 데이터 조회 실패", e);
            return ResponseEntity.status(500).body(
                DebugResponse.<List<DataLabDto>>builder()
                    .success(false)
                    .message("방문자수 데이터 조회 실패: " + e.getMessage())
                    .data(List.of())
                    .build()
            );
        }
    }
}
