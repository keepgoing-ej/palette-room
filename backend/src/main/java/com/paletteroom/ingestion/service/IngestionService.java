package com.paletteroom.ingestion.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.List;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

import org.springframework.stereotype.Service;

import com.paletteroom.artwork.domain.Artwork;
import com.paletteroom.artwork.domain.ArtworkColor;
import com.paletteroom.artwork.repository.ArtworkColorRepository;
import com.paletteroom.artwork.repository.ArtworkRepository;
import com.paletteroom.common.util.DominantColor;
import com.paletteroom.common.util.KmeansColorExtractor;
import com.paletteroom.ingestion.domain.IngestionLog;
import com.paletteroom.ingestion.port.ArtworkSourcePort;
import com.paletteroom.ingestion.port.SourceArtwork;
import com.paletteroom.ingestion.repository.IngestionLogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IngestionService {

    // Spring 마법: ArtworkSourcePort 구현체(@Component) 3개가 List로 전부 주입됨
    // → 어댑터를 추가해도 이 클래스는 무수정 (OCP, 면접 §4)
    private final List<ArtworkSourcePort> sources;
    private final ArtworkRepository artworkRepository;
    private final ArtworkColorRepository artworkColorRepository;
    private final IngestionLogRepository ingestionLogRepository;

    /** 전체 소스 수집. limit = 소스당 가져올 최대 개수 */
    public void ingestAll(int limit) {
        for (ArtworkSourcePort source : sources) {
            ingestSource(source, limit);   // 소스 하나 실패해도 다음 소스 진행 (실패 격리)
        }
    }

    private void ingestSource(ArtworkSourcePort source, int limit) {
        // ① 작업 시작 기록
        IngestionLog log = ingestionLogRepository.save(
                IngestionLog.builder()
                        .source(source.sourceName())
                        .startedAt(java.time.LocalDateTime.now())
                        .build());

        int fetched = 0, saved = 0, skipped = 0, failed = 0;
        try {
            // ② 작품 목록 가져오기
            List<SourceArtwork> artworks = source.fetchArtworks(limit);
            fetched = artworks.size();

            // ③ 작품별 처리 — 개별 실패는 failed++만 하고 계속
            for (SourceArtwork a : artworks) {
                try {
                    if (artworkRepository.existsBySourceAndSourceId(source.sourceName(), a.sourceId())) {
                        skipped++;   // 어제 이미 수집한 작품
                        continue;
                    }
                    saveArtworkWithColors(source.sourceName(), a);
                    saved++;
                } catch (Exception e) {
                    failed++;
                }
            }
            // ④ 성공 마무리 (엔티티 메서드로 값 채우고 다시 save → UPDATE)
            log.success(fetched, saved, skipped, failed);
        } catch (Exception e) {
            // fetch 자체가 실패 (API 장애 등)
            log.fail(e.getMessage());
        }
        ingestionLogRepository.save(log);
    }

    private void saveArtworkWithColors(String sourceName, SourceArtwork a) throws Exception {
        // 이미지 다운로드 (Java 21: new URL(문자열)은 deprecated → URI 경유)
    	BufferedImage image = downloadImage(a.imageUrl());
    	if (image == null) throw new IllegalStateException("이미지 디코딩 실패");

        // k-means로 지배색 추출 (원본은 여기서 쓰고 버림 — 저장 안 함, ADR-06)
        List<DominantColor> colors = KmeansColorExtractor.extract(image);

        // Artwork 저장
        Artwork artwork = artworkRepository.save(Artwork.builder()
                .source(sourceName)
                .sourceId(a.sourceId())
                .title(a.title())
                .artist(a.artist())
                .dateDisplay(a.dateDisplay())
                .medium(a.medium())
                .department(a.department())
                .imageUrl(a.imageUrl())
                .sourceUrl(a.sourceUrl())
                .license("CC0")                     // 3개 소스 모두 오픈액세스 CC0
                .build());

        // 색 5개 저장 — double → BigDecimal 변환은 저장 직전 여기서
        int rank = 1;
        for (DominantColor c : colors) {
            artworkColorRepository.save(ArtworkColor.builder()
                    .artwork(artwork)
                    .colorRank(rank++)
                    .hex(c.hex())
                    .labL(scale2(c.labL()))
                    .labA(scale2(c.labA()))
                    .labB(scale2(c.labB()))
                    .colorRatio(BigDecimal.valueOf(c.ratio()).setScale(4, RoundingMode.HALF_UP))
                    .build());
        }
    }

    private BigDecimal scale2(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP);
    }
    
    // downloadImage 메서드 수정
    private final org.springframework.web.client.RestClient imageClient =
            org.springframework.web.client.RestClient.create();

    private BufferedImage downloadImage(String url) throws Exception {
        byte[] bytes = imageClient.get()
                .uri(URI.create(url))                      // 문자열 아닌 URI로 — 콤마 재인코딩 방지
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36")
                .retrieve()  // 헤더 수정 
                .body(byte[].class);
        if (bytes == null) throw new IllegalStateException("이미지 응답 없음");
        return ImageIO.read(new java.io.ByteArrayInputStream(bytes));
    }
}