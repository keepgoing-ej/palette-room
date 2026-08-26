package com.paletteroom.artwork.dto;

import com.paletteroom.artwork.domain.Artwork;

public record ArtworkDetailResponse(
        Long id,
        String title,
        String artist,
        String dateDisplay,
        String medium,
        String department,
        String imageUrl,
        String sourceUrl,
        String source
) {
    // Entity → 응답 변환기. Service에서 호출
    public static ArtworkDetailResponse from(Artwork artwork) {
        return new ArtworkDetailResponse(
                artwork.getId(),
                artwork.getTitle(),
                artwork.getArtist(),
                artwork.getDateDisplay(),
                artwork.getMedium(),
                artwork.getDepartment(),
                artwork.getImageUrl(),
                artwork.getSourceUrl(),
                artwork.getSource()
        );
    }
}