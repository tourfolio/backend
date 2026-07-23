package com.tourfolio.app.util;

/**
 * 정규화 상수 및 유틸리티 클래스
 * 섹션 03 최신(min-max 정규화) 기준 구현
 */
public final class NormalizationConstants {

    private NormalizationConstants() {
        // 유틸리티 클래스 - 인스턴스화 방지
    }

    // P (관광지 집중률 예측) 정규화 상수
    // 집중률은 피크 대비 상대값(0~100)이므로 상한은 100.0 이다.
    // 91.69는 최댓값이 아니라 범위(100 - 8.31)임에 주의.
    public static final double P_MIN = 8.31;
    public static final double P_MAX = 100.0;
    public static final double P_RANGE = P_MAX - P_MIN;   // = 91.69

    // D (지역별 관광 수요 강도) 정규화 상수
    public static final double D_STAY_MIN = 60.46;
    public static final double D_STAY_MAX = 125.20;
    public static final double D_STAY_RANGE = D_STAY_MAX - D_STAY_MIN;

    public static final double D_SPEND_MIN = 54.09;
    public static final double D_SPEND_MAX = 147.02;
    public static final double D_SPEND_RANGE = D_SPEND_MAX - D_SPEND_MIN;

    // R (지역별 관광 자원 수요) 정규화 상수
    public static final double R_SERVICE_MIN = 63.55;
    public static final double R_SERVICE_MAX = 152.49;
    public static final double R_SERVICE_RANGE = R_SERVICE_MAX - R_SERVICE_MIN;

    public static final double R_CULTURE_MIN = 64.24;
    public static final double R_CULTURE_MAX = 136.21;
    public static final double R_CULTURE_RANGE = R_CULTURE_MAX - R_CULTURE_MIN;

    // FinalChange 범위 상수
    public static final double FINAL_CHANGE_MIN = -0.10;
    public static final double FINAL_CHANGE_MAX = 0.10;

    /**
     * 정규화된 값 clamp (0~1 범위)
     */
    public static double clampNormalized(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    /**
     * 최종 변동률 clamp (-0.10~0.10 범위)
     */
    public static double clampFinalChange(double value) {
        return Math.max(FINAL_CHANGE_MIN, Math.min(FINAL_CHANGE_MAX, value));
    }

    /**
     * P 값 정규화: (P값 - 8.31) / 91.69
     */
    public static double normalizeP(double pValue) {
        double normalized = (pValue - P_MIN) / P_RANGE;
        return clampNormalized(normalized);
    }

    /**
     * D 체류 값 정규화: (체류값 - 60.46) / 64.74
     */
    public static double normalizeDStay(double stayValue) {
        double normalized = (stayValue - D_STAY_MIN) / D_STAY_RANGE;
        return clampNormalized(normalized);
    }

    /**
     * D 소비 값 정규화: (소비값 - 54.09) / 92.93
     */
    public static double normalizeDSpend(double spendValue) {
        double normalized = (spendValue - D_SPEND_MIN) / D_SPEND_RANGE;
        return clampNormalized(normalized);
    }

    /**
     * R 서비스 값 정규화: (서비스값 - 63.55) / 88.94
     */
    public static double normalizeRService(double serviceValue) {
        double normalized = (serviceValue - R_SERVICE_MIN) / R_SERVICE_RANGE;
        return clampNormalized(normalized);
    }

    /**
     * R 문화 값 정규화: (문화값 - 64.24) / 71.97
     */
    public static double normalizeRCulture(double cultureValue) {
        double normalized = (cultureValue - R_CULTURE_MIN) / R_CULTURE_RANGE;
        return clampNormalized(normalized);
    }
}
