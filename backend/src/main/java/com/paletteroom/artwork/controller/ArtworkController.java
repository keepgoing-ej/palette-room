package com.paletteroom.artwork.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.paletteroom.artwork.dto.ColorSearchResponse;
import com.paletteroom.artwork.service.ArtworkService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/artworks")
@RequiredArgsConstructor
public class ArtworkController {
	

	private final ArtworkService artworkService;
	
	@GetMapping("/search")
	public ResponseEntity<List<ColorSearchResponse>> search(
	        @RequestParam String hex,
	        @RequestParam(defaultValue = "20") double tolerance,
	        @RequestParam(defaultValue = "20") int limit) {
	    return ResponseEntity.ok(artworkService.searchByColor(hex, tolerance, limit));
	   }
}