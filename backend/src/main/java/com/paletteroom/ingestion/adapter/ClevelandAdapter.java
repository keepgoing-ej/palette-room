package com.paletteroom.ingestion.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.paletteroom.ingestion.port.ArtworkSourcePort;
import com.paletteroom.ingestion.port.SourceArtwork;

@Component
public class ClevelandAdapter implements ArtworkSourcePort {

    // [1] 이 어댑터가 때릴 API의 공통 주소. 미술관마다 다르니 어댑터마다 자기 것
    private static final String BASE = "https://openaccess-api.clevelandart.org/api";

    // [2] HTTP 호출 도구. 세 어댑터 모두 동일
    private final RestClient restClient = RestClient.create();

    // [3] 이 소스의 이름표. artworks.source 컬럼에 저장될 값
    @Override
    public String sourceName() {
        return "CLEVELAND";
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SourceArtwork> fetchArtworks(int limit) {

        // [4] API 호출: 이미지 있는 작품을 limit개 요청 → JSON을 Map으로 받음
        //     Cleveland는 Chicago처럼 한 번의 호출로 끝 (MET만 2단계였음)
        Map<String, Object> response = restClient.get()
                .uri(BASE + "/artworks/?has_image=1&limit=" + limit)
                .retrieve()
                .body(Map.class);

        // [5] 작품 목록은 "data" 키 아래 배열로 옴 (Chicago와 같은 구조)
        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
        if (data == null) return List.of();

        List<SourceArtwork> result = new ArrayList<>();
        for (Map<String, Object> obj : data) {

            // [6] 이미지 URL 꺼내기 — Cleveland는 3단 중첩: images 맵 > web 맵 > url
            //     어느 단계든 없으면 이 작품은 스킵 (이미지 없는 작품은 색 추출 불가)
            Map<String, Object> images = (Map<String, Object>) obj.get("images");
            if (images == null) continue;
            Map<String, Object> web = (Map<String, Object>) images.get("web");
            if (web == null) continue;
            String imageUrl = (String) web.get("url");
            if (imageUrl == null || imageUrl.isBlank()) continue;

            // [7] 작가 꺼내기 — creators가 "배열"이라 첫 번째 사람의 description을 사용
            //     작가 미상이면 배열이 비어 있음 → null (DB도 artist nullable)
            List<Map<String, Object>> creators = (List<Map<String, Object>>) obj.get("creators");
            String artist = (creators == null || creators.isEmpty())
                    ? null : (String) creators.get(0).get("description");

            // [8] 공통 모델(SourceArtwork)로 변환 — 필드명이 미술관마다 달라서
            //     여기서 "번역"이 일어남. 이게 어댑터의 존재 이유
            result.add(new SourceArtwork(
                    String.valueOf(obj.get("id")),      // 숫자 id → 문자열
                    (String) obj.get("title"),
                    artist,
                    (String) obj.get("creation_date"),  // MET: objectDate / Chicago: date_display
                    (String) obj.get("technique"),      // MET: medium / Chicago: medium_display
                    (String) obj.get("department"),
                    imageUrl,
                    (String) obj.get("url")             // 링크백 (NFR-06)
            ));
        }
        return result;
    }
}