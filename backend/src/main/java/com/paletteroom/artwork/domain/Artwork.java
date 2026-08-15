package com.paletteroom.artwork.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="artworks", uniqueConstraints =
	@UniqueConstraint(name = "uk_artwork_source", columnNames = {"source","source_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Artwork {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false, length = 20)
	private String source;
	
	@Column(nullable = false, length = 50)
	private String sourceId;
	
	@Column(nullable = false, length = 500)
	private String title;
	
	@Column(length = 255)
	private String artist;
	
	@Column(length = 100)
	private String dateDisplay;
	
	@Column(length = 255)
	private String medium;
	
	@Column(length = 100)
	private String department;
	
	@Column(nullable = false, length = 1000)
	private String imageUrl;
	
	@Column(nullable = false, length = 1000)
	private String sourceUrl;
	
	@Column(nullable = false, length = 50)
	private String license;
	
	@Column(nullable = false, name="created_at", updatable = false, insertable = false)
	private LocalDateTime createdAt;
	
	@Column(nullable = false, name="updated_at", updatable = false, insertable = false)
	private LocalDateTime updatedAt;
	
	@Builder
	public Artwork(String source, String sourceId, String title, String artist, String dateDisplay, 
			String medium, String department, String imageUrl, String sourceUrl, String license) {
		this.source = source;
		this.sourceId = sourceId;
		this.title = title;
		this.artist = artist;
		this.dateDisplay = dateDisplay;
		this.medium = medium;
		this.department = department;
		this.imageUrl = imageUrl;
		this.sourceUrl = sourceUrl;
		this.license = license;
		
	}
}
