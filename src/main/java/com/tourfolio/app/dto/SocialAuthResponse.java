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
@Schema(description = "소셜 로그인 응답")
public class SocialAuthResponse {
    
    @Schema(description = "회원 ID")
    private Long id;
    
    @Schema(description = "이메일")
    private String email;
    
    @Schema(description = "닉네임")
    private String nickname;
    
    @Schema(description = "액세스 토큰")
    private String token;
    
    @Schema(description = "가입 여부 (true: 신규 가입, false: 기존 회원)")
    private boolean isNewMember;
    
    @Schema(description = "생성일시")
    private LocalDateTime createdAt;
}
