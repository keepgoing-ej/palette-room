package com.paletteroom.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

import org.junit.jupiter.api.Test;

class KmeansColorExtractorTest {

    @Test
    void 단색_이미지는_1위_색이_전체를_차지한다() {
        BufferedImage image = solidImage(Color.RED, 100, 100);
        List<DominantColor> colors = KmeansColorExtractor.extract(image);

        assertThat(colors.get(0).ratio()).isGreaterThan(0.99);
        assertThat(colors.get(0).hex()).isEqualTo("#FF0000");
    }

    @Test
    void 반반_이미지는_두_색이_각각_절반쯤이다() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 50, 100);
        g.setColor(Color.BLACK);
        g.fillRect(50, 0, 50, 100);
        g.dispose();

        List<DominantColor> colors = KmeansColorExtractor.extract(image);

        assertThat(colors.get(0).ratio()).isBetween(0.4, 0.6);
        assertThat(colors.get(1).ratio()).isBetween(0.4, 0.6);
    }

    @Test
    void ratio_합은_1이다() {
        BufferedImage image = solidImage(Color.BLUE, 80, 60);
        List<DominantColor> colors = KmeansColorExtractor.extract(image);

        double sum = colors.stream().mapToDouble(DominantColor::ratio).sum();
        assertThat(sum).isCloseTo(1.0, org.assertj.core.data.Offset.offset(0.001));
    }

    private BufferedImage solidImage(Color color, int w, int h) {
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, w, h);
        g.dispose();
        return image;
    }
}