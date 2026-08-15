package com.paletteroom.artwork.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="artwork_colors", uniqueConstraints =
@UniqueConstraint(name = "uk_artwork_color_rank", columnNames = {"artwork_id","color_rank"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArtworkColor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "artwork_id", nullable = false)
	private Artwork artwork;
	
	@Column(nullable = false, columnDefinition = "TINYINT")
	private int colorRank;
	
	@Column(nullable = false, length = 7)
	private String hex;
	
	@Column(nullable = false, name = "lab_l")
	private BigDecimal labL;
	
	@Column(nullable = false, name = "lab_a")
	private BigDecimal labA;
	
	@Column(nullable = false, name = "lab_b")
	private BigDecimal labB;
	
	@Column(nullable = false)
	private BigDecimal colorRatio;
	
	@Column(nullable = false, name="created_at", updatable = false, insertable = false)
	private LocalDateTime createdAt;
	
	
	@Builder
	public ArtworkColor (Artwork artwork, int colorRank, String hex, BigDecimal labL, BigDecimal labA, BigDecimal labB, BigDecimal colorRatio) {
		this.artwork = artwork;
		this.colorRank = colorRank;
		this.hex = hex; 
		this.labL = labL;
		this.labA = labA;
		this.labB = labB;
		this.colorRatio = colorRatio;
	}
}
