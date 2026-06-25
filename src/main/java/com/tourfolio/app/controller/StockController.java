package com.tourfolio.app.controller;

import com.tourfolio.app.dto.StockResponse;
import com.tourfolio.app.dto.TradeRequest;
import com.tourfolio.app.dto.MemberAssetResponse;
import com.tourfolio.app.entity.Transaction;
import com.tourfolio.app.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "📈 가상 주식 투자 API 컨트롤러", description = "공공데이터 연동 실시간 관광 전광판 시세 조회 및 유저 자산 체결 관리 엔진")
@Slf4j
@RestController
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @Operation(summary = "실시간 상장 관광지 시세판 전체 조회", description = "현재 상장 거래중인 모든 관광지 자산 종목들의 현재가, 전일가, 등락률, 티어 목록을 실시간으로 가져옵니다.")
    @GetMapping
    public ResponseEntity<List<StockResponse>> getAllStocks() {
        log.info("GET /api/v1/stocks - 전광판 시세조회판 트리거 호출");
        return ResponseEntity.ok(stockService.getAllStocks());
    }

    @Operation(summary = "가상 투자 매수 / 매도 거래 오더 체결", description = "유저 잔고 포인트 제약조건을 확인하여 BUY 시에는 차감 및 포트폴리오 적재, SELL 시에는 평단가 대비 포인트를 실시간 환전 정산합니다.")
    @PostMapping("/trade")
    public ResponseEntity<Transaction> executeTrade(@RequestBody TradeRequest request) {
        log.info("POST /api/v1/stocks/trade - 가상 체결 시스템 오더 수신: {}", request);
        Transaction tx = stockService.executeTrade(request);
        return ResponseEntity.ok(tx);
    }

    @Operation(summary = "유저 포인트 및 개인 투자 포트폴리오 자산 스크리너 조회", description = "마이페이지 또는 투자 탭 메인에 연동할 유저 전용 API입니다. 현재 포인트 잔액, 총 주식 평가액, 누적 투자 수익률(%) 및 보유 종목 리스트를 정밀 정산 보고합니다.")
    @GetMapping("/assets")
    public ResponseEntity<MemberAssetResponse> getMemberAssets(
            @Parameter(description = "조회할 유저 고유 일련번호 (테스트 기본 유저 식별 ID는 1번입니다.)", example = "1")
            @RequestParam Long memberId) {
        log.info("GET /api/v1/stocks/assets - 유저 포트폴리오 자산 스크리너 조회 ID: {}", memberId);
        return ResponseEntity.ok(stockService.getMemberAssets(memberId));
    }

    @Operation(summary = "공공데이터 장애 방어용 가상 메트릭스 모의 데이터 조회", description = "한국관광공사 허브망 오픈 API가 트래픽 차단 등으로 튕겼을 때 정상 시뮬레이션을 지속하기 위한 랜덤 통계 인자 분석용 내부 메트릭스를 조회합니다.")
    @GetMapping("/test-api")
    public ResponseEntity<Map<String, Object>> testOpenApiMetrics(
            @Parameter(description = "관광지 종목 일련번호 고유 식별자", example = "1")
            @RequestParam Long spotId) {
        return ResponseEntity.ok(stockService.testOpenApiMetrics(spotId));
    }
}