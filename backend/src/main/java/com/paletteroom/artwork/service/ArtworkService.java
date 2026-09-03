package com.paletteroom.artwork.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paletteroom.artwork.domain.Artwork;
import com.paletteroom.artwork.dto.ArtworkDetailResponse;
import com.paletteroom.artwork.dto.ColorSearchResponse;
import com.paletteroom.artwork.repository.ArtworkColorRepository;
import com.paletteroom.artwork.repository.ArtworkRepository;
import com.paletteroom.common.exception.BusinessException;
import com.paletteroom.common.exception.ErrorCode;
import com.paletteroom.common.util.ColorConverter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArtworkService {

    private final ArtworkColorRepository artworkColorRepository;
    private final ArtworkRepository artworkRepository; // 8/26추가

    @Transactional(readOnly = true) // category 추가 
    public List<ColorSearchResponse> searchByColor(String hex, double tolerance, int limit, String category) {
        if (hex == null || !hex.matches("#[0-9A-Fa-f]{6}")) {
            throw new BusinessException(ErrorCode.INVALID_COLOR_FORMAT);
        }
        double[] lab = ColorConverter.hexToLab(hex);
        return artworkColorRepository.searchByLab(lab[0], lab[1], lab[2], tolerance, limit, category)
                .stream()
                .map(ColorSearchResponse::from)
                .toList();
    }
    
    
    // 상세 조회: id로 찾고, 없으면 404 8/26추가
    @Transactional(readOnly = true)
    public ArtworkDetailResponse getArtwork(Long id) {
        Artwork artwork = artworkRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTWORK_NOT_FOUND));
        return ArtworkDetailResponse.from(artwork);
    }
   
    // 목록 조회: 페이지 단위로 잘라서
    @Transactional(readOnly = true)
    public Page<ArtworkDetailResponse> getArtworks(Pageable pageable) {
        return artworkRepository.findAll(pageable)
                .map(ArtworkDetailResponse::from);
    }
}