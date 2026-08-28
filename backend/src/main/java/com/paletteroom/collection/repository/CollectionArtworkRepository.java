package com.paletteroom.collection.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paletteroom.collection.domain.CollectionArtwork;

public interface CollectionArtworkRepository extends JpaRepository<CollectionArtwork, Long> {
	
	// 8/28 추가 
	boolean existsByCollectionIdAndArtworkId(Long collectionId, Long artworkId);
	Optional<CollectionArtwork> findByCollectionIdAndArtworkId(Long collectionId, Long artworkId);
	List<CollectionArtwork> findAllByCollectionId(Long collectionId);

}
