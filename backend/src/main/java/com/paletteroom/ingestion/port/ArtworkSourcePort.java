package com.paletteroom.ingestion.port;

import java.util.List;

public interface ArtworkSourcePort {
    String sourceName();                       // "MET" 등
    List<SourceArtwork> fetchArtworks(int limit);
}