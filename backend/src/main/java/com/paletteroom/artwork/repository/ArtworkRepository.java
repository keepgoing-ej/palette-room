package com.paletteroom.artwork.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paletteroom.artwork.domain.Artwork;

public interface ArtworkRepository extends JpaRepository<Artwork, Long>{
	
	// 중복검사
	boolean existsBySourceAndSourceId(String source, String sourceId);
}
