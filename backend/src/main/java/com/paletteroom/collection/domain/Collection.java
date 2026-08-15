package com.paletteroom.collection.domain;

import java.time.LocalDateTime;

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
@Table(name = "collections")
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class Collection {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user; 
	
	@Column(nullable = false, length = 50)
	private String name;
	
	@Column(nullable = false, length = 7)
	private String themeColor;
	
	@Column(nullable = false, insertable = false, updatable = false)
	private LocalDateTime createdAt;
	
	@Column(nullable = false, insertable = false, updatable = false)
	private LocalDateTime updatedAt; 
	
	@Builder
	public Collection (User user, String name, String themeColor) {
		this.user = user;
		this.name = name;
		this.themeColor = themeColor;
	}
}
