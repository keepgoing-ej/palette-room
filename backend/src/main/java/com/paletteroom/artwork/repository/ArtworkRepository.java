package com.paletteroom.artwork.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.paletteroom.artwork.domain.Artwork;

public interface ArtworkRepository extends JpaRepository<Artwork, Long>{

	// 중복검사
	boolean existsBySourceAndSourceId(String source, String sourceId);

	// [변경] 제목 검색 — 제목에 키워드가 든 작품(대소문자 무시), 페이지 단위
	Page<Artwork> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
}