package com.tourfolio.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "인증 응답 DTO")
public class AuthResponse {

    @Schema(description = "사용자 ID", example = "1")
    private Long id;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "닉네임", example = "투어폴리오유저")
    private String nickname;

    @Schema(description = "액세스 토큰 (MVP 단계에서는 임시 토큰)", example = "temp_token_12345")
    private String token;

    @Schema(description = "생성 시간")
    private LocalDateTime createdAt;
}
