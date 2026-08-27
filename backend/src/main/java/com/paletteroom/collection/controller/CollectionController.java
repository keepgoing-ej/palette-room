package com.paletteroom.collection.controller;

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

import com.paletteroom.collection.dto.CollectionRequest;
import com.paletteroom.collection.dto.CollectionResponse;
import com.paletteroom.collection.service.CollectionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/collections")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    // 생성: POST /api/collections — 201
    @PostMapping
    public ResponseEntity<CollectionResponse> create(
            @AuthenticationPrincipal Long userId,          // 토큰에서 나온 내 id (필터가 넣어준 것)
            @RequestBody CollectionRequest request) {
        CollectionResponse response = collectionService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 내 목록: GET /api/collections — 200
    @GetMapping
    public ResponseEntity<List<CollectionResponse>> getMyCollections(
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(collectionService.getMyCollections(userId));
    }

    // 수정: PATCH /api/collections/{id} — 200
    @PatchMapping("/{id}")
    public ResponseEntity<CollectionResponse> update(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestBody CollectionRequest request) {
        return ResponseEntity.ok(collectionService.update(userId, id, request));
    }

    // 삭제: DELETE /api/collections/{id} — 204
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        collectionService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
}