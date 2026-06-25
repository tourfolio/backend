package com.tourfolio.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenApiDto {

    @JsonProperty("response")
    private Response response;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        @JsonProperty("header")
        private Header header;

        @JsonProperty("body")
        private Body body;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Header {
        @JsonProperty("resultCode")
        private String resultCode;

        @JsonProperty("resultMsg")
        private String resultMsg;

        @JsonProperty("successYN")
        private String successYN;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Body {
        @JsonProperty("items")
        private Items items;

        @JsonProperty("numOfRows")
        private Integer numOfRows;

        @JsonProperty("pageNo")
        private Integer pageNo;

        @JsonProperty("totalCount")
        private Integer totalCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Items {
        @JsonProperty("item")
        private List<Item> item;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        @JsonProperty("contentid")
        private String contentId;

        @JsonProperty("contenttypeid")
        private String contentTypeId;

        @JsonProperty("title")
        private String title;

        @JsonProperty("addr1")
        private String addr1;

        @JsonProperty("addr2")
        private String addr2;

        @JsonProperty("areacode")
        private String areaCode;

        @JsonProperty("sigungucode")
        private String sigunguCode;

        @JsonProperty("cat1")
        private String cat1;

        @JsonProperty("cat2")
        private String cat2;

        @JsonProperty("cat3")
        private String cat3;

        @JsonProperty("createdtime")
        private String createdTime;

        @JsonProperty("modifiedtime")
        private String modifiedTime;

        @JsonProperty("readcount")
        private Integer readCount;

        @JsonProperty("thumbnail")
        private String thumbnail;

        @JsonProperty("mapx")
        private String mapX;

        @JsonProperty("mapy")
        private String mapY;

        @JsonProperty("mlevel")
        private String mLevel;

        @JsonProperty("overview")
        private String overview;

        @JsonProperty("homepage")
        private String homepage;

        @JsonProperty("tel")
        private String tel;

        @JsonProperty("telname")
        private String telName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VisitorResponse {
        @JsonProperty("response")
        private VisitorResponseInner response;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class VisitorResponseInner {
            @JsonProperty("header")
            private VisitorHeader header;

            @JsonProperty("body")
            private VisitorBody body;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class VisitorHeader {
            @JsonProperty("resultCode")
            private String resultCode;

            @JsonProperty("resultMsg")
            private String resultMsg;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class VisitorBody {
            @JsonProperty("items")
            private VisitorItems items;

            @JsonProperty("numOfRows")
            private Integer numOfRows;

            @JsonProperty("pageNo")
            private Integer pageNo;

            @JsonProperty("totalCount")
            private Integer totalCount;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class VisitorItems {
            @JsonProperty("item")
            private List<VisitorItem> item;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class VisitorItem {
            @JsonProperty("contentid")
            private String contentId;

            @JsonProperty("foreignVisitorCnt")
            private Integer foreignVisitorCnt;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DemandResponse {
        @JsonProperty("response")
        private DemandResponseInner response;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DemandResponseInner {
            @JsonProperty("header")
            private DemandHeader header;

            @JsonProperty("body")
            private DemandBody body;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DemandHeader {
            @JsonProperty("resultCode")
            private String resultCode;

            @JsonProperty("resultMsg")
            private String resultMsg;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DemandBody {
            @JsonProperty("items")
            private DemandItems items;

            @JsonProperty("numOfRows")
            private Integer numOfRows;

            @JsonProperty("pageNo")
            private Integer pageNo;

            @JsonProperty("totalCount")
            private Integer totalCount;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DemandItems {
            @JsonProperty("item")
            private List<DemandItem> item;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DemandItem {
            @JsonProperty("contentid")
            private String contentId;

            @JsonProperty("stayTimeMin")
            private Integer stayTimeMin;

            @JsonProperty("spendMoneyWon")
            private Integer spendMoneyWon;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceResponse {
        @JsonProperty("response")
        private ResourceResponseInner response;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ResourceResponseInner {
            @JsonProperty("header")
            private ResourceHeader header;

            @JsonProperty("body")
            private ResourceBody body;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ResourceHeader {
            @JsonProperty("resultCode")
            private String resultCode;

            @JsonProperty("resultMsg")
            private String resultMsg;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ResourceBody {
            @JsonProperty("items")
            private ResourceItems items;

            @JsonProperty("numOfRows")
            private Integer numOfRows;

            @JsonProperty("pageNo")
            private Integer pageNo;

            @JsonProperty("totalCount")
            private Integer totalCount;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ResourceItems {
            @JsonProperty("item")
            private List<ResourceItem> item;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ResourceItem {
            @JsonProperty("contentid")
            private String contentId;

            @JsonProperty("snsMentionCnt")
            private Integer snsMentionCnt;

            @JsonProperty("cultureSearchCnt")
            private Integer cultureSearchCnt;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForecastResponse {
        @JsonProperty("response")
        private ForecastResponseInner response;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ForecastResponseInner {
            @JsonProperty("header")
            private ForecastHeader header;

            @JsonProperty("body")
            private ForecastBody body;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ForecastHeader {
            @JsonProperty("resultCode")
            private String resultCode;

            @JsonProperty("resultMsg")
            private String resultMsg;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ForecastBody {
            @JsonProperty("items")
            private ForecastItems items;

            @JsonProperty("numOfRows")
            private Integer numOfRows;

            @JsonProperty("pageNo")
            private Integer pageNo;

            @JsonProperty("totalCount")
            private Integer totalCount;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ForecastItems {
            @JsonProperty("item")
            private List<ForecastItem> item;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ForecastItem {
            @JsonProperty("contentid")
            private String contentId;

            @JsonProperty("predictFocusRate")
            private String predictFocusRate;
        }
    }
}