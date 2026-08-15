package com.paletteroom.note.domain;

import java.time.LocalDateTime;

import com.paletteroom.artwork.domain.Artwork;
import com.paletteroom.user.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name="notes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Note {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "artwork_id")
	private Artwork artwork;
	
	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;
	
	@Column(length = 7)
	private String moodColor;
	
	@Column(nullable = false, name="created_at", updatable = false, insertable = false)
	private LocalDateTime createdAt;
	
	@Column(nullable = false, name="updated_at", updatable = false, insertable = false)
	private LocalDateTime updatedAt; 
	
	@Builder
	public Note (User user, Artwork artwork, String content, String moodColor) {
			this.user = user;
			this.artwork = artwork;
			this.content = content;
			this.moodColor = moodColor;
	}
}
