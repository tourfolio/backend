package com.tourfolio.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
    
    @Schema(description = "카카오 인가 코드", example = "authorization_code_from_kakao")
    private String code;
}
