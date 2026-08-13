package com.tourfolio.app.controller;

import com.tourfolio.app.dto.AnnouncementDetailResponse;
import com.tourfolio.app.dto.AnnouncementResponse;
import com.tourfolio.app.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/announcements")
@RequiredArgsConstructor
@Tag(name = "공지사항", description = "공지사항 목록/상세 조회 API")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    @Operation(summary = "공지사항 목록 조회")
    public ResponseEntity<List<AnnouncementResponse>> getAnnouncements() {
        log.info("GET /api/v1/announcements - 공지사항 목록 조회");
        return ResponseEntity.ok(announcementService.getAnnouncements());
    }

    @GetMapping("/{id}")
    @Operation(summary = "공지사항 상세 조회")
    public ResponseEntity<AnnouncementDetailResponse> getAnnouncement(@PathVariable Long id) {
        log.info("GET /api/v1/announcements/{} - 공지사항 상세 조회", id);
        return ResponseEntity.ok(announcementService.getAnnouncement(id));
    }
}