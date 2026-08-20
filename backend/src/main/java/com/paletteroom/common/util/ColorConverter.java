package com.paletteroom.common.util;

public class ColorConverter {

    private ColorConverter() {
        // 유틸 클래스 — 인스턴스 생성 방지
    }

    /** HEX(#RRGGBB) → RGB 배열 [r, g, b] (0~255) */
    public static int[] hexToRgb(String hex) {
        String value = hex.replace("#", "");
        int r = Integer.parseInt(value.substring(0, 2), 16);
        int g = Integer.parseInt(value.substring(2, 4), 16);
        int b = Integer.parseInt(value.substring(4, 6), 16);
        return new int[]{r, g, b};
    }

    /** RGB(0~255) → LAB 배열 [L, a, b] — sRGB → XYZ(D65) → CIELAB 표준 변환 */
    public static double[] rgbToLab(int r, int g, int b) {
        // 1. 감마 보정 (sRGB → linear)
        double rl = pivotRgb(r / 255.0);
        double gl = pivotRgb(g / 255.0);
        double bl = pivotRgb(b / 255.0);

        // 2. linear RGB → XYZ (sRGB 변환 행렬)
        double x = rl * 0.4124 + gl * 0.3576 + bl * 0.1805;
        double y = rl * 0.2126 + gl * 0.7152 + bl * 0.0722;
        double z = rl * 0.0193 + gl * 0.1192 + bl * 0.9505;

        // 3. D65 백색점 정규화
        x /= 0.95047;
        y /= 1.00000;
        z /= 1.08883;

        // 4. XYZ → LAB
        double fx = pivotXyz(x);
        double fy = pivotXyz(y);
        double fz = pivotXyz(z);

        double l = 116 * fy - 16;
        double a = 500 * (fx - fy);
        double bb = 200 * (fy - fz);
        return new double[]{l, a, bb};
    }

    /** HEX → LAB 한 번에 */
    public static double[] hexToLab(String hex) {
        int[] rgb = hexToRgb(hex);
        return rgbToLab(rgb[0], rgb[1], rgb[2]);
    }

    private static double pivotRgb(double v) {
        return (v > 0.04045) ? Math.pow((v + 0.055) / 1.055, 2.4) : v / 12.92;
    }

    private static double pivotXyz(double v) {
        return (v > 0.008856) ? Math.cbrt(v) : (7.787 * v) + (16.0 / 116.0);
    }
}