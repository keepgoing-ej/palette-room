package com.paletteroom.collection.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paletteroom.collection.domain.Collection;

public interface CollectionRepository extends JpaRepository<Collection, Long>{
	List<Collection> findAllByUserId(Long userId);
	long countByUserId(Long userId);

}
