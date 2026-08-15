package com.paletteroom.collection.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paletteroom.collection.domain.Collection;

public interface CollectionRepository extends JpaRepository<Collection, Long>{

}
