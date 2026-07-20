package com.tourfolio.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebugResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private Map<String, Object> metadata;
}
