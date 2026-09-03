package com.tourfolio.app.service;

import com.tourfolio.app.dto.CollectionDetailResponse;
import com.tourfolio.app.dto.CollectionListResponse;
import com.tourfolio.app.entity.Collection;
import com.tourfolio.app.entity.CollectionSpot;
import com.tourfolio.app.entity.Spot;
import com.tourfolio.app.exception.CustomException;
import com.tourfolio.app.repository.CollectionRepository;
import com.tourfolio.app.repository.CollectionSpotRepository;
import com.tourfolio.app.repository.SpotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollectionExploreService {

    private final CollectionRepository collectionRepository;
    private final CollectionSpotRepository collectionSpotRepository;
    private final SpotRepository spotRepository;

    public List<CollectionListResponse> getCollections() {
        return collectionRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(c -> CollectionListResponse.builder()
                        .collectionId(c.getId())
                        .title(c.getTitle())
                        .thumbnailUrl(c.getThumbnailUrl())
                        .placeCount(collectionSpotRepository.countByCollectionId(c.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    public CollectionDetailResponse getCollectionDetail(Long collectionId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new CustomException("COLLECTION_NOT_FOUND", "컬렉션을 찾을 수 없습니다."));

        List<CollectionSpot> collectionSpots = collectionSpotRepository.findByCollectionIdOrderByDisplayOrderAsc(collectionId);

        List<CollectionDetailResponse.CollectionSpotItem> items = collectionSpots.stream()
                .map(cs -> {
                    Spot spot = spotRepository.findById(cs.getSpotId()).orElse(null);
                    if (spot == null) return null;
                    return CollectionDetailResponse.CollectionSpotItem.builder()
                            .spotId(spot.getId())
                            .name(spot.getName())
                            .imageUrl(spot.getImageUrl())
                            .build();
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        return CollectionDetailResponse.builder()
                .collectionId(collection.getId())
                .title(collection.getTitle())
                .placeCount(items.size())
                .spots(items)
                .build();
    }
}