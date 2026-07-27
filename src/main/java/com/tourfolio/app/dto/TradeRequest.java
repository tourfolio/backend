package com.tourfolio.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "주식 거래 요청 DTO")
public class TradeRequest {

    @Schema(description = "회원 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "회원 ID는 필수 항목입니다.")
    private Long memberId;

    @Schema(description = "관광지(주식) ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "관광지 ID는 필수 항목입니다.")
    private Long spotId;

    @Schema(description = "거래 유형 (BUY: 매수, SELL: 매도)", example = "BUY", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "거래 유형은 필수 항목입니다.")
    private String type;

    @Schema(description = "거래 수량", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "거래 수량은 필수 항목입니다.")
    @DecimalMin(value = "0.01", message = "거래 수량은 0.01 이상이어야 합니다.")
    private BigDecimal quantity;
}