package com.paletteroom.artwork.dto;

import java.math.BigDecimal;

import com.paletteroom.artwork.repository.ColorSearchRow;

public record ColorSearchResponse(
        Long artworkId, String hex, BigDecimal colorRatio, Double distance,
        String title, String artist, String imageUrl) {

	public static ColorSearchResponse from(ColorSearchRow row) {
	    return new ColorSearchResponse(
	        row.getArtWorkId(), row.getHex(), row.getColorRatio(), row.getDist(),
	        row.getTitle(), row.getArtist(), row.getImageUrl());
	}
}
