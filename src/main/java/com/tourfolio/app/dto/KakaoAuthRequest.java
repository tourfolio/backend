package com.tourfolio.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "카카오 소셜 로그인 요청 (네이티브 SDK로 발급받은 카카오 액세스 토큰)")
public class KakaoAuthRequest {

    @Schema(description = "카카오 액세스 토큰 (프론트에서 카카오 SDK로 발급)", example = "access_token_from_kakao_sdk", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "카카오 액세스 토큰은 필수 항목입니다.")
    private String accessToken;
}