package com.tourfolio.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KakaoUserInfoResponse {
    
    private Long id;
    
    @JsonProperty("connected_at")
    private String connectedAt;

    private Map<String, Object> properties;

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KakaoAccount {
        
        private Boolean profileNeedsAgreement;
        
        private Boolean emailNeedsAgreement;
        
        private Boolean ageRangeNeedsAgreement;
        
        private Boolean birthdayNeedsAgreement;
        
        private Boolean birthyearNeedsAgreement;
        
        private Boolean genderNeedsAgreement;
        
        private Boolean ciNeedsAgreement;
        
        private Profile profile;
        
        private String email;
        
        private String ageRange;
        
        private String birthday;
        
        private String birthyear;
        
        private String gender;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Profile {
        
        private String nickname;
        
        private String thumbnailImageUrl;
        
        private String profileImageUrl;
        
        private Boolean isDefaultImage;
    }
}
