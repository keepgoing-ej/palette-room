package com.paletteroom.artwork.repository;

import java.math.BigDecimal;

public interface ColorSearchRow {
	Long getArtWorkId();
	String getHex();
	BigDecimal getColorRatio();
	Double getDist();
    String getTitle();      // 추가
    String getArtist();     // 추가
    String getImageUrl();   // 추가
}
