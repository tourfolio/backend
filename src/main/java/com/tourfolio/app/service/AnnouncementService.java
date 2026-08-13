package com.tourfolio.app.service;

import com.tourfolio.app.dto.AnnouncementDetailResponse;
import com.tourfolio.app.dto.AnnouncementResponse;
import com.tourfolio.app.entity.Announcement;
import com.tourfolio.app.exception.CustomException;
import com.tourfolio.app.repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    public List<AnnouncementResponse> getAnnouncements() {
        return announcementRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(a -> AnnouncementResponse.builder()
                        .id(a.getId())
                        .title(a.getTitle())
                        .createdAt(a.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    public AnnouncementDetailResponse getAnnouncement(Long id) {
        Announcement a = announcementRepository.findById(id)
                .orElseThrow(() -> new CustomException("ANNOUNCEMENT_NOT_FOUND", "공지사항을 찾을 수 없습니다."));

        return AnnouncementDetailResponse.builder()
                .id(a.getId())
                .title(a.getTitle())
                .content(a.getContent())
                .createdAt(a.getCreatedAt())
                .build();
    }
}