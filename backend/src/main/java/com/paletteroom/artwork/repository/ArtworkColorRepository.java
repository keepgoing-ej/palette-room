package com.paletteroom.artwork.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.paletteroom.artwork.domain.ArtworkColor;

public interface ArtworkColorRepository extends JpaRepository<ArtworkColor, Long> {
	@Query(value = """
	        SELECT ac.artwork_id AS artworkId,
	               MIN(ac.hex) AS hex,
	               MAX(ac.color_ratio) AS colorRatio,
	               MIN(POW(ac.lab_l - :l, 2) + POW(ac.lab_a - :a, 2) + POW(ac.lab_b - :b, 2)) AS dist,
	               MIN(a.title) AS title,
	               MIN(a.artist) AS artist,
	               MIN(a.image_url) AS imageUrl
	        FROM artwork_colors ac
	        JOIN artworks a ON a.id = ac.artwork_id
	        WHERE ac.lab_l BETWEEN :l - :tol AND :l + :tol
	          AND ac.lab_a BETWEEN :a - :tol AND :a + :tol
	          AND ac.lab_b BETWEEN :b - :tol AND :b + :tol
	          AND ac.color_ratio >= 0.15
	          AND (:category IS NULL OR a.category = :category)
	        GROUP BY ac.artwork_id
	        HAVING dist <= :tol * :tol
	        ORDER BY dist
	        LIMIT :limit
	        """, nativeQuery = true)
	List<ColorSearchRow> searchByLab(@Param("l") double l,
	                                 @Param("a") double a,
	                                 @Param("b") double b,
	                                 @Param("tol") double tol,
	                                 @Param("limit") int limit,
	                                 @Param("category") String category);
}