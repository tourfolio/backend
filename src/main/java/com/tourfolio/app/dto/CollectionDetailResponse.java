package com.tourfolio.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectionDetailResponse {
    private Long collectionId;
    private String title;
    private Integer placeCount;
    private List<CollectionSpotItem> spots;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CollectionSpotItem {
        private Long spotId;
        private String name;
        private String imageUrl;
    }
}