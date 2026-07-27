package com.tourfolio.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "자산 충전 요청 DTO")
public class ChargeRequest {

    @Schema(description = "회원 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "회원 ID는 필수 항목입니다.")
    private Long memberId;

    @Schema(description = "충전 금액", example = "100000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "충전 금액은 필수 항목입니다.")
    @DecimalMin(value = "0.01", message = "충전 금액은 0.01 이상이어야 합니다.")
    private BigDecimal amount;
}
