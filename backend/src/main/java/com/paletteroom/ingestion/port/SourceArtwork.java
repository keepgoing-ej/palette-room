package com.paletteroom.ingestion.port;

public record SourceArtwork(
        String sourceId,
        String title,
        String artist,
        String dateDisplay,
        String medium,
        String department,
        String imageUrl,
        String sourceUrl
) {
}
