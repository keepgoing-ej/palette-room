package com.paletteroom.artwork.repository;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class ArtworkColorRepositoryTest {
	
	/*
    @Autowired
    public ArtworkColorRepositoryTest() {
		// TODO Auto-generated constructor stub
	} ArtworkColorRepository;*/

	@Autowired
	ArtworkColorRepository artworkColorRepository;
	
    @Test
    void 갈색조_검색() {				//클래스는 대문자, 주고받는 객체는 소문자 시작 
        List<ColorSearchRow> rows = artworkColorRepository.searchByLab(50, 15, 25, 20, 10);
        rows.forEach(r -> System.out.println(r.getArtWorkId() + " | " + r.getHex() + " | " + r.getDist()));
    }	
}