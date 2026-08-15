package com.paletteroom.artwork.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paletteroom.artwork.domain.ArtworkColor;

public interface ArtworkColorRepository extends JpaRepository<ArtworkColor, Long> {

}
