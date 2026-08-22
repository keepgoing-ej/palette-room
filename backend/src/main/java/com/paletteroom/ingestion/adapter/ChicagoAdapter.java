package com.paletteroom.ingestion.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.paletteroom.ingestion.port.ArtworkSourcePort;
import com.paletteroom.ingestion.port.SourceArtwork;

@Component
public class ChicagoAdapter implements ArtworkSourcePort {

    private static final String BASE = "https://api.artic.edu/api/v1";

    private final RestClient restClient = RestClient.create();

    @Override
    public String sourceName() {
        return "CHICAGO";
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SourceArtwork> fetchArtworks(int limit) {
        // MET와 달리 한 번의 호출로 목록+상세를 같이 받음
        Map<String, Object> response = restClient.get()
                .uri(BASE + "/artworks/search?query[term][is_public_domain]=true"
                        + "&fields=id,title,artist_display,date_display,medium_display,department_title,image_id"
                        + "&limit=" + limit)
                .retrieve()
                .body(Map.class);

        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
        if (data == null) return List.of();

        List<SourceArtwork> result = new ArrayList<>();
        for (Map<String, Object> obj : data) {
            String imageId = (String) obj.get("image_id");
            if (imageId == null || imageId.isBlank()) continue; // 이미지 없으면 스킵

            String id = String.valueOf(obj.get("id"));
            result.add(new SourceArtwork(
                    id,
                    (String) obj.get("title"),
                    (String) obj.get("artist_display"),
                    (String) obj.get("date_display"),
                    (String) obj.get("medium_display"),
                    (String) obj.get("department_title"),
                    // Chicago는 image_id로 IIIF URL을 직접 조립
                    "https://www.artic.edu/iiif/2/" + imageId + "/full/400,/0/default.jpg",
                    "https://www.artic.edu/artworks/" + id
            ));
        }
        return result;
    }
}