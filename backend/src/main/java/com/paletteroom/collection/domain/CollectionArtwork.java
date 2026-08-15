package com.paletteroom.collection.domain;

import java.time.LocalDateTime;

import com.paletteroom.artwork.domain.Artwork;

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
@Getter
@Table(name = "collection_artworks", uniqueConstraints =
@UniqueConstraint(name = "uk_collection_artwork", columnNames = {"collection_id","artwork_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class CollectionArtwork {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "collection_id", nullable = false)
	private Collection collection;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "artwork_id", nullable = false)
	private Artwork artwork;
	
	@Column(name = "added_at", nullable = false, insertable = false, updatable = false)
	private LocalDateTime addedAt;

	@Builder
	public CollectionArtwork (Collection collection, Artwork artwork) {
		this.collection = collection;
		this.artwork = artwork;
	}

}
