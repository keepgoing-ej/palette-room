package com.paletteroom.note.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.paletteroom.note.dto.NoteRequest;
import com.paletteroom.note.dto.NoteResponse;
import com.paletteroom.note.service.NoteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    // 작성: POST /api/notes — 201
    @PostMapping
    public ResponseEntity<NoteResponse> create(
            @AuthenticationPrincipal Long userId,
            @RequestBody NoteRequest request) {
        NoteResponse response = noteService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 내 목록: GET /api/notes — 200
    @GetMapping
    public ResponseEntity<List<NoteResponse>> getMyNotes(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(noteService.getMyNotes(userId));
    }

    // 수정: PATCH /api/notes/{id} — 200
    @PatchMapping("/{id}")
    public ResponseEntity<NoteResponse> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestBody NoteRequest request) {
        return ResponseEntity.ok(noteService.update(userId, id, request));
    }

    // 삭제: DELETE /api/notes/{id} — 204
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        noteService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}