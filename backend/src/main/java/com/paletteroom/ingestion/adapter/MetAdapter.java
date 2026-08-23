package com.paletteroom.ingestion.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.paletteroom.ingestion.port.ArtworkSourcePort;
import com.paletteroom.ingestion.port.SourceArtwork;

@Component
public class MetAdapter implements ArtworkSourcePort {

    private static final String BASE = "https://collectionapi.metmuseum.org/public/collection/v1";

    private final RestClient restClient = RestClient.create();

    @Override
    public String sourceName() {
        return "MET";
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SourceArtwork> fetchArtworks(int limit) {
        // 1단계: 이미지 있는 회화 작품 ID 목록
        Map<String, Object> search = restClient.get() //부서 필터 추가 후 다시 조정(범위 넓힘) q=* 수정
        		.uri(BASE + "/search?hasImages=true&departmentId=11&q=portrait")
        		.retrieve()
                .body(Map.class);

        List<Integer> ids = (List<Integer>) search.get("objectIDs");
        if (ids == null) return List.of();

        // 2단계: ID별 상세 조회 → 공통 모델 변환
        List<SourceArtwork> result = new ArrayList<>();
        int tried = 0;                                        // ← 추가 ①
        for (Integer id : ids) {
            if (result.size() >= limit) break;
            tried++;                                          // ← 추가 ②

            try {
                Map<String, Object> obj = restClient.get()
                        .uri(BASE + "/objects/" + id)
                        .retrieve()
                        .body(Map.class);

                String imageUrl = (String) obj.get("primaryImageSmall");
                if (imageUrl == null || imageUrl.isBlank()) continue; // 이미지 없으면 스킵

                result.add(new SourceArtwork(
                        String.valueOf(id),
                        (String) obj.get("title"),
                        (String) obj.get("artistDisplayName"),
                        (String) obj.get("objectDate"),
                        (String) obj.get("medium"),
                        (String) obj.get("department"),
                        imageUrl,
                        (String) obj.get("objectURL")
                ));
            } catch (Exception e) {
                // 개별 작품 실패는 스킵 (실패 격리 — 면접 §4)
            }
        }
        System.out.println("MET tried=" + tried + " → collected=" + result.size());  // ← 추가 ③
        return result;
        
        
    }
}