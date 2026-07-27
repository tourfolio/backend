// src/main/java/com/tourfolio/app/controller/WatchlistController.java
package com.tourfolio.app.controller;

import com.tourfolio.app.dto.WatchlistResponse;
import com.tourfolio.app.entity.Watchlist;
import com.tourfolio.app.service.WatchlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/watchlist")
@RequiredArgsConstructor
@Tag(name = "관심목록", description = "회원의 관심 관광 주식 등록, 삭제, 조회 API")
public class WatchlistController {

    private final WatchlistService watchlistService;

    @PostMapping("/{spotId}")
    @Operation(summary = "관심목록 등록", description = "회원이 특정 관광 주식을 관심목록에 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "관심목록 등록 성공"),
            @ApiResponse(responseCode = "400", description = "이미 등록된 종목 또는 유효하지 않은 요청"),
            @ApiResponse(responseCode = "404", description = "회원 또는 주식 종목을 찾을 수 없음")
    })
    public ResponseEntity<Watchlist> addToWatchlist(
            @PathVariable Long spotId,
            @RequestParam Long memberId) {
        log.info("POST /api/watchlist/{} - 관심 등록: memberId={}", spotId, memberId);
        try {
            Watchlist watchlist = watchlistService.addToWatchlist(memberId, spotId);
            return ResponseEntity.ok(watchlist);
        } catch (Exception e) {
            log.error("관심 등록 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{spotId}")
    @Operation(summary = "관심목록 삭제", description = "회원의 관심목록에서 특정 관광 주식을 제거합니다.")
    public ResponseEntity<Void> removeFromWatchlist(
            @PathVariable Long spotId,
            @RequestParam Long memberId) {
        log.info("DELETE /api/watchlist/{} - 관심 해제: memberId={}", spotId, memberId);
        try {
            watchlistService.removeFromWatchlist(memberId, spotId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("관심 해제 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping
    @Operation(summary = "관심목록 조회", description = "회원이 등록한 관심 관광 주식 목록을 조회합니다.")
    public ResponseEntity<List<WatchlistResponse>> getWatchlist(@RequestParam Long memberId) {
        log.info("GET /api/watchlist - 내 관심 목록 조회: memberId={}", memberId);
        List<WatchlistResponse> watchlist = watchlistService.getWatchlist(memberId);
        return ResponseEntity.ok(watchlist);
    }
}
