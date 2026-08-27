package com.paletteroom.collection.dto;

import com.paletteroom.collection.domain.Collection;

public record CollectionResponse(Long id, String name, String themeColor) {
	
	public static CollectionResponse from (Collection collection) {
		return new CollectionResponse(
				collection.getId(),
				collection.getName(),
				collection.getThemeColor()
				);
	}
		
}
