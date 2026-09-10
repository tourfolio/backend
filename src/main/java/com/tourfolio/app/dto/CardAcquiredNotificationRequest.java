// src/main/java/com/tourfolio/app/dto/CardAcquiredNotificationRequest.java
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
@Schema(description = "카드 획득 알림 생성 요청 DTO")
public class CardAcquiredNotificationRequest {

    @Schema(description = "카드(관광지) 이름", example = "경복궁", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "카드 이름은 필수 항목입니다.")
    private String cardName;
}