package com.tourfolio.app.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 공공데이터포털 표준 API 응답 포맷 Wrapper
 * response > header > resultCode/resultMsg
 * response > body > items > item
 */
@Data
public class PublicApiResponse<T> {
    @JsonProperty("response")
    private Response<T> response;

    @Data
    public static class Response<T> {
        @JsonProperty("header")
        private Header header;

        @JsonProperty("body")
        private Body<T> body;
    }

    @Data
    public static class Header {
        @JsonProperty("resultCode")
        private String resultCode;

        @JsonProperty("resultMsg")
        private String resultMsg;
    }

    @Data
    public static class Body<T> {
        @JsonProperty("items")
        private Items<T> items;

        @JsonProperty("numOfRows")
        private Integer numOfRows;

        @JsonProperty("pageNo")
        private Integer pageNo;

        @JsonProperty("totalCount")
        private Integer totalCount;
    }

    @Data
    public static class Items<T> {
        @JsonProperty("item")
        private List<T> item;
    }

    public boolean isSuccess() {
        return response != null && response.header != null && "00".equals(response.header.resultCode);
    }

    public List<T> getItems() {
        if (response != null && response.body != null && response.body.items != null) {
            return response.body.items.item;
        }
        return List.of();
    }
}
