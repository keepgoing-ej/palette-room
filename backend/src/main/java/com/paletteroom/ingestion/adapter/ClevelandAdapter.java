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

    // [1] 이 어댑터가 때릴 API의 공통 주소
    private static final String BASE = "https://openaccess-api.clevelandart.org/api";

    // [2] HTTP 호출 도구
    private final RestClient restClient = RestClient.create();

    // [3] 이 소스의 이름표. artworks.source 컬럼에 저장될 값
    @Override
    public String sourceName() {
        return "CLEVELAND";
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SourceArtwork> fetchArtworks(int limit) {
        List<SourceArtwork> result = new ArrayList<>();
        int skip = 0;   // [4] 페이지 위치. 0 → 1000 → 2000 ... 으로 넘어감

        // [5] Cleveland API는 한 번에 최대 1000개 → limit 채울 때까지 반복 호출
        while (result.size() < limit) {
            int batch = Math.min(1000, limit - result.size());  // 이번에 요청할 개수

            Map<String, Object> response = restClient.get()
                    .uri(BASE + "/artworks/?has_image=1&limit=" + batch + "&skip=" + skip)
                    .retrieve()
                    .body(Map.class);

            List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
            if (data == null || data.isEmpty()) break;   // 더 이상 데이터 없음 → 반복 종료

            skip += data.size();   // 다음 페이지 위치로 이동

            // [6] ★이 for문은 while "안"에 있어야 함 — 매 페이지마다 파싱하니까
            for (Map<String, Object> obj : data) {

                // [7] 이미지 URL — 3단 중첩: images 맵 > web 맵 > url
                //     어느 단계든 없으면 스킵 (이미지 없으면 색 추출 불가)
                Map<String, Object> images = (Map<String, Object>) obj.get("images");
                if (images == null) continue;
                Map<String, Object> web = (Map<String, Object>) images.get("web");
                if (web == null) continue;
                String imageUrl = (String) web.get("url");
                if (imageUrl == null || imageUrl.isBlank()) continue;

                // [8] 작가 — creators 배열의 첫 요소 description. 미상이면 null
                List<Map<String, Object>> creators = (List<Map<String, Object>>) obj.get("creators");
                String artist = (creators == null || creators.isEmpty())
                        ? null : (String) creators.get(0).get("description");

                // [9] 공통 모델로 번역
                result.add(new SourceArtwork(
                        String.valueOf(obj.get("id")),
                        (String) obj.get("title"),
                        artist,
                        (String) obj.get("creation_date"),
                        (String) obj.get("technique"),
                        (String) obj.get("department"),
                        imageUrl,
                        (String) obj.get("url")
                ));
            }
            // ← for 끝. while은 계속 돌 수 있음
        }
        // ← while 끝
        return result;   // [10] return은 메서드 맨 끝, 반복 다 끝난 뒤 한 번만
    }
}