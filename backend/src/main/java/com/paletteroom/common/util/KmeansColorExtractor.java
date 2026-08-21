package com.paletteroom.common.util;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class KmeansColorExtractor {

    private static final int K = 5;              // 지배색 개수
    private static final int MAX_ITERATIONS = 20; // 반복 상한
    private static final int TARGET_SIZE = 200;   // 리사이즈 긴 변

    private KmeansColorExtractor() {
    }

    /** 이미지에서 지배색 K개를 점유율 내림차순으로 추출 */
    public static List<DominantColor> extract(BufferedImage image) {
        // 1. 리사이즈 (색 비율 유지, 픽셀 수 절감)
        BufferedImage resized = resize(image);

        // 2. 전체 픽셀을 [r,g,b] 점 목록으로
        int w = resized.getWidth();
        int h = resized.getHeight();
        List<int[]> points = new ArrayList<>(w * h);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = resized.getRGB(x, y);
                points.add(new int[]{(rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF});
            }
        }

        // 3. 초기 중심: 랜덤 픽셀 K개 (seed 고정 → 결과 재현 가능)
        Random random = new Random(42);
        double[][] centers = new double[K][3];
        for (int i = 0; i < K; i++) {
            int[] p = points.get(random.nextInt(points.size()));
            centers[i] = new double[]{p[0], p[1], p[2]};
        }

        int[] assignment = new int[points.size()];

        // 4. 할당 ↔ 갱신 반복
        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            boolean changed = false;

            // [할당] 각 점을 가장 가까운 중심으로
            for (int i = 0; i < points.size(); i++) {
                int nearest = nearestCenter(points.get(i), centers);
                if (assignment[i] != nearest) {
                    assignment[i] = nearest;
                    changed = true;
                }
            }
            if (!changed) break; // 수렴

            // [갱신] 각 클러스터의 평균색으로 중심 이동
            double[][] sums = new double[K][3];
            int[] counts = new int[K];
            for (int i = 0; i < points.size(); i++) {
                int c = assignment[i];
                int[] p = points.get(i);
                sums[c][0] += p[0];
                sums[c][1] += p[1];
                sums[c][2] += p[2];
                counts[c]++;
            }
            for (int c = 0; c < K; c++) {
                if (counts[c] > 0) {
                    centers[c][0] = sums[c][0] / counts[c];
                    centers[c][1] = sums[c][1] / counts[c];
                    centers[c][2] = sums[c][2] / counts[c];
                }
            }
        }

        // 5. 클러스터 크기 집계 → DominantColor 변환, ratio 내림차순
        int[] counts = new int[K];
        for (int a : assignment) counts[a]++;

        List<DominantColor> result = new ArrayList<>();
        for (int c = 0; c < K; c++) {
            if (counts[c] == 0) continue; // 빈 클러스터(단색 이미지 등)는 제외
            int r = (int) Math.round(centers[c][0]);
            int g = (int) Math.round(centers[c][1]);
            int b = (int) Math.round(centers[c][2]);
            String hex = String.format("#%02X%02X%02X", r, g, b);
            double[] lab = ColorConverter.rgbToLab(r, g, b);
            double ratio = (double) counts[c] / points.size();
            result.add(new DominantColor(hex, lab[0], lab[1], lab[2], ratio));
        }
        result.sort(Comparator.comparingDouble(DominantColor::ratio).reversed());
        return result;
    }

    /** 긴 변 TARGET_SIZE로 축소 (이미 작으면 그대로) */
    private static BufferedImage resize(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        if (Math.max(w, h) <= TARGET_SIZE) return src;

        double scale = (double) TARGET_SIZE / Math.max(w, h);
        int nw = (int) (w * scale);
        int nh = (int) (h * scale);
        BufferedImage out = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.drawImage(src, 0, 0, nw, nh, null);
        g.dispose();
        return out;
    }

    /** 유클리드 거리(제곱)로 가장 가까운 중심 인덱스 */
    private static int nearestCenter(int[] p, double[][] centers) {
        int nearest = 0;
        double min = Double.MAX_VALUE;
        for (int c = 0; c < centers.length; c++) {
            double dr = p[0] - centers[c][0];
            double dg = p[1] - centers[c][1];
            double db = p[2] - centers[c][2];
            double dist = dr * dr + dg * dg + db * db; // sqrt 생략 (비교만 하니까)
            if (dist < min) {
                min = dist;
                nearest = c;
            }
        }
        return nearest;
    }
}