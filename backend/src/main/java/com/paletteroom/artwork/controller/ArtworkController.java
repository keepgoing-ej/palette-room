package com.paletteroom.artwork.controller;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.paletteroom.artwork.dto.ArtworkDetailResponse;
import com.paletteroom.artwork.dto.ColorSearchResponse;
import com.paletteroom.artwork.service.ArtworkService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/artworks")
@RequiredArgsConstructor
public class ArtworkController {
	

	private final ArtworkService artworkService;
	
	@GetMapping("/search") // category 추가 
	public ResponseEntity<List<ColorSearchResponse>> search(
	        @RequestParam String hex,
	        @RequestParam(defaultValue = "20") double tolerance,
	        @RequestParam(defaultValue = "20") int limit,
	        @RequestParam(required = false) String category) {
	    return ResponseEntity.ok(artworkService.searchByColor(hex, tolerance, limit, category));
	   }
	
	// 상세: /api/artworks/5217 처럼 경로에 id가 들어오는 형태
	@GetMapping("/{id}")
	public ResponseEntity<ArtworkDetailResponse> getArtwork(@PathVariable Long id) {
	    return ResponseEntity.ok(artworkService.getArtwork(id));
	}

	// 목록: /api/artworks?page=0&size=20
	@GetMapping
	public ResponseEntity<Page<ArtworkDetailResponse>> getArtworks(Pageable pageable) {
	    return ResponseEntity.ok(artworkService.getArtworks(pageable));
	}
}