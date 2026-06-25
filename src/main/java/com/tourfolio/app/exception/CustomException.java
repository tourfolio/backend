package com.tourfolio.app.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final String errorCode;

    public CustomException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}