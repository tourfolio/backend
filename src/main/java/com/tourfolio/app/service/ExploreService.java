// src/main/java/com/tourfolio/app/service/ExploreService.java

package com.tourfolio.app.service;



import com.tourfolio.app.dto.*;

import com.tourfolio.app.entity.Spot;

import com.tourfolio.app.exception.CustomException;

import com.tourfolio.app.repository.SpotRepository;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;



import java.util.Arrays;

import java.util.List;

import java.util.stream.Collectors;



@Service

@RequiredArgsConstructor

@Slf4j

public class ExploreService {



    private final SpotRepository spotRepository;

    private static final String DEFAULT_IMAGE_URL = "https://via.placeholder.com/800x600?text=No+Image";



    private String getImageUrlWithFallback(Spot spot) {
        return spot.getImageUrl() != null && !spot.getImageUrl().isEmpty() ? spot.getImageUrl() : DEFAULT_IMAGE_URL;
    }

    private List<String> parseTags(String themeTag) {
        if (themeTag == null || themeTag.trim().isEmpty()) {
            return List.of();
        }
        // 쉼표로 구분된 태그를 리스트로 변환
        List<String> tags = Arrays.stream(themeTag.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .toList();

        // 태그가 여전히 비어있으면 빈 리스트 반환
        return tags.isEmpty() ? List.of() : tags;
    }



    public List<ExploreResponse> getAllExploreCards() {

        log.info("탐색 화면 전체 카드 조회 시작");

        List<Spot> spots = spotRepository.findAllByOrderByTierAscNameAsc();

        List<ExploreResponse> responses = spots.stream()

                .map(this::mapToExploreResponse)

                .collect(Collectors.toList());

        log.info("탐색 화면 전체 카드 조회 완료: {}건", responses.size());

        return responses;

    }



    public List<ExploreResponse> searchExploreCards(String keyword, String areaCode, String themeTag) {

        log.info("탐색 화면 복합 필터링 조회 시작: keyword={}, areaCode={}, themeTag={}", keyword, areaCode, themeTag);

        

        String normalizedKeyword = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();

        String normalizedAreaCode = (areaCode == null || areaCode.trim().isEmpty()) ? null : areaCode.trim();

        String normalizedThemeTag = (themeTag == null || themeTag.trim().isEmpty()) ? null : themeTag.trim();

        

        List<Spot> spots = spotRepository.searchExploreCards(normalizedKeyword, normalizedAreaCode, normalizedThemeTag);

        List<ExploreResponse> responses = spots.stream()

                .map(this::mapToExploreResponse)

                .collect(Collectors.toList());

        log.info("탐색 화면 복합 필터링 조회 완료: {}건", responses.size());

        return responses;

    }



    private ExploreResponse mapToExploreResponse(Spot spot) {

        return ExploreResponse.builder()

                .id(spot.getId())

                .name(spot.getName())

                .areaCode(spot.getAreaCode())

                .areaName(spot.getAreaName() != null ? spot.getAreaName() : spot.getRegion())

                .themeTag(spot.getThemeTag() != null ? spot.getThemeTag() : spot.getTheme())

                .tier(spot.getTier())

                .imageUrl(getImageUrlWithFallback(spot))

                .description(spot.getDescription())

                .mapX(spot.getMapX())

                .mapY(spot.getMapY())

                .address(spot.getAddress() != null ? spot.getAddress() : "")

                .tags(parseTags(spot.getThemeTag()))

                .build();

    }

    // 신규: 풀스크린 메인 카드 조회 (Editor's Pick)
    public List<MainCardResponse> getMainCards() {
        log.info("풀스크린 메인 카드 조회 시작");
        List<Spot> spots = spotRepository.findMainCards();
        List<MainCardResponse> responses = spots.stream()
                .limit(6) // Editor's Pick 최대 6개
                .map((spot) -> {
                    int index = spots.indexOf(spot) + 1;
                    return MainCardResponse.builder()
                            .spotId(spot.getId())
                            .name(spot.getName())
                            .subTitle(generateSubTitle(spot))
                            .description(spot.getDescription())
                            .location(spot.getAreaName() != null ? spot.getAreaName() : spot.getRegion())
                            .address(spot.getAddress() != null ? spot.getAddress() : "")
                            .imageUrl(getImageUrlWithFallback(spot))
                            .theme(spot.getTheme() != null ? spot.getTheme() : "")
                            .tags(parseTags(spot.getThemeTag()))
                            .totalCount(Math.min(spots.size(), 6))
                            .currentIndex(index)
                            .build();
                })
                .collect(Collectors.toList());
        log.info("풀스크린 메인 카드 조회 완료: {}건", responses.size());
        return responses;
    }

    // 신규: 컨텐츠 허브 조회
    public HubResponse getHubData() {

        log.info("컨텐츠 허브 조회 시작");



        // 테마 목록 (하드코딩된 예시 데이터 - 실제로는 테마 테이블에서 조회 필요)

        List<ThemeCard> themes = Arrays.asList(

                ThemeCard.builder()

                        .themeId(1L)

                        .title("벚꽃 따라 떠나는 봄 여행")

                        .placeCount(12)

                        .imageUrl("https://example.com/images/cherry-blossom.jpg")

                        .build(),

                ThemeCard.builder()

                        .themeId(2L)

                        .title("역사 속으로 걷는 문화 여행")

                        .placeCount(8)

                        .imageUrl("https://example.com/images/history.jpg")

                        .build(),

                ThemeCard.builder()

                        .themeId(3L)

                        .title("바다와 함께하는 레저 여행")

                        .placeCount(15)

                        .imageUrl("https://example.com/images/beach.jpg")

                        .build()

        );



        // 인기/추천 관광지 (조회수 기반)

        List<Spot> trendingSpots = spotRepository.findTrendingSpotsByViewCount();

        List<TrendingSpot> trending = trendingSpots.stream()

                .limit(5)

                .map(spot -> {

                    int index = trendingSpots.indexOf(spot) + 1;

                    return TrendingSpot.builder()

                            .spotId(spot.getId())

                            .name(spot.getName())

                            .location(spot.getAreaName() != null ? spot.getAreaName() : spot.getRegion())

                            .popularityRank(index)

                            .imageUrl(getImageUrlWithFallback(spot))

                            .build();

                })

                .collect(Collectors.toList());



        log.info("컨텐츠 허브 조회 완료: 테마 {}건, 인기 {}건", themes.size(), trending.size());

        return HubResponse.builder()

                .themes(themes)

                .trendingSpots(trending)

                .build();

    }



    // 신규: 테마별 관광지 목록 조회

    public ThemeDetailResponse getThemeDetail(String themeId) {

        log.info("테마별 관광지 목록 조회 시작: themeId={}", themeId);



        // 테마 ID를 테마 이름으로 변환 (간단한 매핑 - 실제로는 테마 테이블 필요)

        String themeName = mapThemeIdToName(themeId);

        if (themeName == null) {

            throw new CustomException("THEME_NOT_FOUND", "테마를 찾을 수 없습니다.");

        }



        List<Spot> spots = spotRepository.findByThemeOrderByTier(themeName);

        List<ThemeSpotSummary> spotSummaries = spots.stream()

                .map(spot -> ThemeSpotSummary.builder()

                        .spotId(spot.getId())

                        .name(spot.getName())

                        .location(spot.getAreaName() != null ? spot.getAreaName() : spot.getRegion())

                        .imageUrl(getImageUrlWithFallback(spot))

                        .tags(Arrays.asList(spot.getThemeTag() != null ? spot.getThemeTag() : spot.getTheme()))

                        .build())

                .collect(Collectors.toList());



        log.info("테마별 관광지 목록 조회 완료: {}건", spotSummaries.size());

        return ThemeDetailResponse.builder()

                .themeId(Long.parseLong(themeId))

                .title(themeName + " 여행")

                .description(themeName + " 테마의 관광지를 소개합니다.")

                .imageUrl("https://example.com/images/" + themeName + ".jpg")

                .spots(spotSummaries)

                .build();

    }



    // 신규: 관광지 상세 정보 조회

    @Transactional

    public SpotDetailResponse getSpotDetail(Long spotId) {

        log.info("관광지 상세 정보 조회 시작: spotId={}", spotId);



        Spot spot = spotRepository.findById(spotId)

                .orElseThrow(() -> new CustomException("SPOT_NOT_FOUND", "관광지를 찾을 수 없습니다."));



        // 조회수 증가 (null-safe 처리)
        Long currentViewCount = spot.getViewCount() != null ? spot.getViewCount() : 0L;
        spot.setViewCount(currentViewCount + 1);
        spotRepository.save(spot);



        // 주변 관광지 조회 (region이 null인 경우 빈 리스트 반환)
        List<Spot> nearbySpots = spot.getRegion() != null
                ? spotRepository.findNearbySpots(spot.getRegion(), spotId)
                : List.of();

        List<NearbySpot> nearby = nearbySpots.stream()

                .limit(4)

                .map(s -> NearbySpot.builder()

                        .spotId(s.getId() != null ? s.getId() : 0L)

                        .name(s.getName() != null ? s.getName() : "")

                        .imageUrl(getImageUrlWithFallback(s))

                        .build())

                .collect(Collectors.toList());



        // 매력 포인트 (하드코딩된 예시 - 실제로는 별도 테이블 필요)

        List<AttractionPoint> attractionPoints = Arrays.asList(

                AttractionPoint.builder()

                        .title("야경 감상")

                        .iconType("moon")

                        .iconUrl("https://example.com/icons/moon.png")

                        .build(),

                AttractionPoint.builder()

                        .title("역사 체험")

                        .iconType("history")

                        .iconUrl("https://example.com/icons/history.png")

                        .build()

        );



        log.info("관광지 상세 정보 조회 완료: spotId={}", spotId);

        // Null-safe fallback 처리
        String address = spot.getAddress() != null ? spot.getAddress() : (spot.getRegion() != null ? spot.getRegion() : "");
        String description = spot.getDescription() != null ? spot.getDescription() : "";
        List<String> tags = parseTags(spot.getThemeTag());

        return SpotDetailResponse.builder()

                .spotId(spot.getId() != null ? spot.getId() : 0L)

                .name(spot.getName() != null ? spot.getName() : "")

                .address(address)

                .tags(tags != null ? tags : List.of())

                .description(description)

                .operatingHours("09:00 - 18:00 (계절별 변동)")

                .closedDays("매주 화요일")

                .admissionFee("성인 3,000원")

                .website("https://www.example.com")

                .phoneNumber("02-1234-5678")

                .attractionPoints(attractionPoints)

                .nearbySpots(nearby)

                .build();

    }



    // 신규: 복합 필터링 검색 (totalCount 포함)

    public SearchResponse searchSpotsWithTotalCount(String keyword, List<String> regions, List<String> themes, List<String> tags) {

        log.info("복합 필터링 검색 시작: keyword={}, regions={}, themes={}, tags={}", keyword, regions, themes, tags);



        String normalizedKeyword = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();

        List<String> normalizedRegions = (regions == null || regions.isEmpty()) ? null : regions;

        List<String> normalizedThemes = (themes == null || themes.isEmpty()) ? null : themes;

        List<String> normalizedTags = (tags == null || tags.isEmpty()) ? null : tags;



        List<Spot> spots = spotRepository.searchSpotsWithFilters(normalizedKeyword, normalizedRegions, normalizedThemes);

        // 다중 태그 필터 추가 적용 (OR 조건: 태그 중 하나라도 매칭되면 포함)
        if (normalizedTags != null && !normalizedTags.isEmpty()) {
            spots = spots.stream()
                    .filter(spot -> spot.getThemeTag() != null && normalizedTags.stream()
                            .anyMatch(tag -> spot.getThemeTag().toLowerCase().contains(tag.toLowerCase())))
                    .collect(Collectors.toList());
        }

        List<SearchResponse.SearchSpotItem> items = spots.stream()

                .map(spot -> SearchResponse.SearchSpotItem.builder()

                        .spotId(spot.getId())

                        .name(spot.getName())

                        .location(spot.getAreaName() != null ? spot.getAreaName() : spot.getRegion())

                        .address(spot.getAddress() != null ? spot.getAddress() : "")

                        .imageUrl(getImageUrlWithFallback(spot))

                        .tags(Arrays.asList(spot.getThemeTag() != null ? spot.getThemeTag() : spot.getTheme()))

                        .build())

                .collect(Collectors.toList());



        log.info("복합 필터링 검색 완료: {}건", items.size());

        return SearchResponse.builder()

                .spots(items)

                .totalCount(items.size())

                .build();

    }



    // 헬퍼 메서드: 서브 타이틀 생성

    private String generateSubTitle(Spot spot) {

        if (spot.getTheme() != null) {

            switch (spot.getTheme()) {

                case "역사":

                    return "역사의 숨결이 느껴지는 공간";

                case "자연":

                    return "자연의 아름다움을 품은 곳";

                case "레저":

                    return "즐거움이 가득한 공간";

                case "문화":

                    return "문화적 가치가 빛나는 곳";

                default:

                    return "특별한 여행을 위한 공간";

            }

        }

        return "특별한 여행을 위한 공간";

    }



    // 신규: 지금 뜨는 여행지 조회 (조회수 기준)
    public List<ExploreResponse> getTrendingSpots() {
        log.info("지금 뜨는 여행지 조회 시작 (조회수 기준)");
        try {
            List<Spot> trendingSpots = spotRepository.findTrendingSpotsByViewCount();
            List<ExploreResponse> responses = trendingSpots.stream()
                    .limit(10)
                    .map(this::mapToExploreResponse)
                    .collect(Collectors.toList());
            log.info("지금 뜨는 여행지 조회 완료: {}건", responses.size());
            return responses;
        } catch (Exception e) {
            log.error("지금 뜨는 여행지 조회 실패: error={}", e.getMessage());
            // 예외 발생 시 빈 목록 반환하여 전체 탐색 조회가 다운되지 않도록 안전장치
            return List.of();
        }
    }

    // 헬퍼 메서드: 테마 ID를 테마 이름으로 변환

    private String mapThemeIdToName(String themeId) {

        switch (themeId) {

            case "1":

                return "벚꽃";

            case "2":

                return "역사";

            case "3":

                return "레저";

            default:

                return null;

        }

    }

}

