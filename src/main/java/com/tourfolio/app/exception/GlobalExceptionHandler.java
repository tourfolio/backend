package com.tourfolio.app.exception;

import com.tourfolio.app.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 1. 개발자가 의도적으로 발생시킨 비즈니스 가상 투자 제약조건 예외 처리 파이프라인
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
        log.warn("⚠️ Business Exception Occurred: [{}] {}", ex.getErrorCode(), ex.getMessage());
        ErrorResponse response = ErrorResponse.builder()
                .errorCode(ex.getErrorCode())
                .errorMessage(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        if ("MEMBER_NOT_FOUND".equals(ex.getErrorCode()) || "SPOT_NOT_FOUND".equals(ex.getErrorCode())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 2. [보안 및 디버깅 가드 추가] 브라우저가 자동으로 요청하는 favicon.ico 등 정적 리소스 부재 예외 가로채기
     * 콘솔창에 무의미한 지저분한 StackTrace를 남기지 않도록 가볍게 처리하여 공모전 시연 신뢰성을 확보합니다.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException ex) {
        log.debug("🌐 API Static Resource Not Found (e.g., favicon.ico): {}", ex.getResourcePath());
        ErrorResponse response = ErrorResponse.builder()
                .errorCode("RESOURCE_NOT_FOUND")
                .errorMessage("API 서버에서 정적 리소스를 제공하지 않습니다: " + ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * 3. 시스템 런타임 최상위 예외 마스터 처리기
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error("❌ Unexpected System Exception Triggered: ", ex);
        ErrorResponse response = ErrorResponse.builder()
                .errorCode("INTERNAL_SERVER_ERROR")
                .errorMessage("서버 내부망 코어 연산 오류가 발생했습니다.")
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.internalServerError().body(response);
    }
}