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
@Schema(description = "카카오 소셜 로그인 요청")
public class KakaoAuthRequest {

    @Schema(description = "카카오 인가 코드", example = "authorization_code_from_kakao", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "카카오 인가 코드는 필수 항목입니다.")
    private String code;
}
