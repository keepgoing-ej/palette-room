package com.paletteroom.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ColorConverterTest {

    @Test
    void 흰색은_L100_a0_b0() {
        double[] lab = ColorConverter.hexToLab("#FFFFFF");
        assertThat(lab[0]).isCloseTo(100.0, org.assertj.core.data.Offset.offset(0.5));
        assertThat(lab[1]).isCloseTo(0.0, org.assertj.core.data.Offset.offset(0.5));
        assertThat(lab[2]).isCloseTo(0.0, org.assertj.core.data.Offset.offset(0.5));
    }

    @Test
    void 검은색은_L0() {
        double[] lab = ColorConverter.hexToLab("#000000");
        assertThat(lab[0]).isCloseTo(0.0, org.assertj.core.data.Offset.offset(0.5));
    }

    @Test
    void 빨강은_a가_크게_양수() {
        double[] lab = ColorConverter.hexToLab("#FF0000");
        assertThat(lab[1]).isGreaterThan(50);  // 적록축 +
    }
}