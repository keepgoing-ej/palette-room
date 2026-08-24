package com.paletteroom.artwork.repository;

import java.math.BigDecimal;

public interface ColorSearchRow {
	Long getArtWorkId();
	String getHex();
	BigDecimal getColorRatio();
	Double getDist();
}
