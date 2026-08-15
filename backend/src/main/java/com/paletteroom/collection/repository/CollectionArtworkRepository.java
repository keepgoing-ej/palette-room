package com.paletteroom.collection.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paletteroom.collection.domain.CollectionArtwork;

public interface CollectionArtworkRepository extends JpaRepository<CollectionArtwork, Long> {

}
