package com.paletteroom.note.dto;

public record NoteRequest(String content, String moodColor, Long artworkId) {
}