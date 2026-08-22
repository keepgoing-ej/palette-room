package com.paletteroom.ingestion.adapter;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.paletteroom.ingestion.port.SourceArtwork;

class ClevelandAdapterTest {

    @Test
    void Cleveland에서_작품_3개_가져오기() {
    	ClevelandAdapter adapter = new ClevelandAdapter();
        List<SourceArtwork> artworks = adapter.fetchArtworks(3);

        System.out.println("가져온 작품 수: " + artworks.size());
        artworks.forEach(a -> System.out.println(a.title() + " | " + a.artist() + " | " + a.imageUrl()));
    }

}