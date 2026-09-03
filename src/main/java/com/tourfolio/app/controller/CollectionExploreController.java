package com.tourfolio.app.controller;

import com.tourfolio.app.dto.CollectionDetailResponse;
import com.tourfolio.app.dto.CollectionListResponse;
import com.tourfolio.app.service.CollectionExploreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/explore/collections")
@RequiredArgsConstructor
@Tag(name = "지역별 맞춤 여행지 (컬렉션)", description = "탐색 화면 - 유네스코 세계유산, 국립공원 등 테마별 관광지 모음")
public class CollectionExploreController {

    private final CollectionExploreService collectionExploreService;

    @GetMapping
    @Operation(summary = "컬렉션 목록 조회", description = "지역별 맞춤 여행지 목록을 조회합니다.")
    public ResponseEntity<List<CollectionListResponse>> getCollections() {
        return ResponseEntity.ok(collectionExploreService.getCollections());
    }

    @GetMapping("/{collectionId}")
    @Operation(summary = "컬렉션 상세 조회", description = "특정 컬렉션에 속한 관광지 목록을 조회합니다.")
    public ResponseEntity<CollectionDetailResponse> getCollectionDetail(@PathVariable Long collectionId) {
        return ResponseEntity.ok(collectionExploreService.getCollectionDetail(collectionId));
    }
}