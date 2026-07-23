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

    /**
     * 공공데이터포털은 정상 응답 코드로 "0000"을 내려준다.
     * (일부 레거시 서비스는 "00"을 쓰므로 둘 다 허용)
     */
    public boolean isSuccess() {
        if (response == null || response.header == null) {
            return false;
        }
        String code = response.header.resultCode;
        return "0000".equals(code) || "00".equals(code);
    }

    /** 진단 로그용 결과 코드 (응답 자체가 없으면 "no-response") */
    public static String resultCodeOf(PublicApiResponse<?> response) {
        if (response == null || response.response == null || response.response.header == null) {
            return "no-response";
        }
        return response.response.header.resultCode;
    }

    public List<T> getItems() {
        if (response != null && response.body != null
                && response.body.items != null && response.body.items.item != null) {
            return response.body.items.item;
        }
        return List.of();
    }
}
