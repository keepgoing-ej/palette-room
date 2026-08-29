package com.paletteroom.note.dto;

import com.paletteroom.note.domain.Note;

public record NoteResponse(Long id, String content, String moodColor, Long artworkId, String artworkTitle) {
	
	public static NoteResponse from(Note note) {
	    return new NoteResponse(
	            note.getId(),
	            note.getContent(),
	            note.getMoodColor(),
	            note.getArtwork() == null ? null : note.getArtwork().getId(),
	            note.getArtwork() == null ? null : note.getArtwork().getTitle()
	    );
	}
	
}
