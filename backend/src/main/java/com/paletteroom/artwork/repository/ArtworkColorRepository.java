package com.paletteroom.artwork.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.paletteroom.artwork.domain.ArtworkColor;

public interface ArtworkColorRepository extends JpaRepository<ArtworkColor, Long> {
	@Query(value = """
	        SELECT ac.artwork_id AS artworkId,
	               ac.hex AS hex,
	               ac.color_ratio AS colorRatio,
	               (POW(ac.lab_l - :l, 2) + POW(ac.lab_a - :a, 2) + POW(ac.lab_b - :b, 2)) AS dist
	        FROM artwork_colors ac
	        WHERE ac.lab_l BETWEEN :l - :tol AND :l + :tol
	          AND ac.lab_a BETWEEN :a - :tol AND :a + :tol
	          AND ac.lab_b BETWEEN :b - :tol AND :b + :tol
	        HAVING dist <= :tol * :tol
	        ORDER BY dist
	        LIMIT :limit
	        """, nativeQuery = true)
	List<ColorSearchRow> searchByLab(@Param("l") double l,
	                                 @Param("a") double a,
	                                 @Param("b") double b,
	                                 @Param("tol") double tol,
	                                 @Param("limit") int limit);
}
