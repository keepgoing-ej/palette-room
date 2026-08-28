package com.paletteroom.collection.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paletteroom.artwork.domain.Artwork;
import com.paletteroom.artwork.dto.ArtworkDetailResponse;
import com.paletteroom.artwork.repository.ArtworkRepository;
import com.paletteroom.collection.domain.Collection;
import com.paletteroom.collection.domain.CollectionArtwork;
import com.paletteroom.collection.dto.BulkAddRequest;
import com.paletteroom.collection.dto.BulkAddResponse;
import com.paletteroom.collection.dto.CollectionRequest;
import com.paletteroom.collection.dto.CollectionResponse;
import com.paletteroom.collection.repository.CollectionArtworkRepository;
import com.paletteroom.collection.repository.CollectionRepository;
import com.paletteroom.common.exception.BusinessException;
import com.paletteroom.common.exception.ErrorCode;
import com.paletteroom.user.domain.User;
import com.paletteroom.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private static final int MAX_COLLECTIONS = 20;

    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;
    // 벌크담기 추가 
    private final CollectionArtworkRepository collectionArtworkRepository;
    private final ArtworkRepository artworkRepository;
    
    // 생성: 20개 제한 검사 → 저장
    @Transactional
    public CollectionResponse create(Long userId, CollectionRequest request) {
        if (collectionRepository.countByUserId(userId) >= MAX_COLLECTIONS) {
            throw new BusinessException(ErrorCode.COLLECTION_LIMIT_EXCEEDED);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Collection collection = collectionRepository.save(Collection.builder()
                .user(user)
                .name(request.name())
                .themeColor(request.themeColor())
                .build());
        return CollectionResponse.from(collection);
    }

    // 내 목록
    @Transactional(readOnly = true)
    public List<CollectionResponse> getMyCollections(Long userId) {
        return collectionRepository.findAllByUserId(userId)
                .stream()
                .map(CollectionResponse::from)
                .toList();
    }

    // 수정: 존재 확인 → ★소유권 검증★ → 변경
    @Transactional
    public CollectionResponse update(Long userId, Long collectionId, CollectionRequest request) {
        Collection collection = getOwnedCollection(userId, collectionId);
        collection.update(request.name(), request.themeColor());
        return CollectionResponse.from(collection);
        // save() 호출이 없는 이유: @Transactional 안에서 엔티티를 바꾸면
        // JPA가 변경을 감지해 커밋 시점에 알아서 UPDATE (더티 체킹 — 면접 카드)
    }

    // 삭제: 존재 확인 → 소유권 검증 → 삭제
    @Transactional
    public void delete(Long userId, Long collectionId) {
        Collection collection = getOwnedCollection(userId, collectionId);
        collectionRepository.delete(collection);
    }

    // 공통: "존재하고, 내 것인" 컬렉션만 통과
    private Collection getOwnedCollection(Long userId, Long collectionId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COLLECTION_NOT_FOUND));
        if (!collection.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.COLLECTION_ACCESS_DENIED);   // 403 — IDOR 방어
        }
        return collection;
    }
    
    // 벌크 담기
 // 벌크 담기: 중복은 skip, 없는 작품 id도 skip — 실패시키지 않는다 (ADR-28)
    @Transactional
    public BulkAddResponse addArtworks(Long userId, Long collectionId, BulkAddRequest request) {
        Collection collection = getOwnedCollection(userId, collectionId);   // 소유권 먼저

        int added = 0, skipped = 0;
        for (Long artworkId : request.artworkIds()) {
            // 이미 담겨 있으면 skip (1차 방어 — 최종 방어는 DB UNIQUE)
            if (collectionArtworkRepository.existsByCollectionIdAndArtworkId(collectionId, artworkId)) {
                skipped++;
                continue;
            }
            // 존재하지 않는 작품 id면 skip
            Artwork artwork = artworkRepository.findById(artworkId).orElse(null);
            if (artwork == null) {
                skipped++;
                continue;
            }
            collectionArtworkRepository.save(CollectionArtwork.builder()
                    .collection(collection)
                    .artwork(artwork)
                    .build());
            added++;
        }
        return new BulkAddResponse(added, skipped);
    }

    // 빼기: 컬렉션 소유권 → 그 안에 그 작품이 있는지 → 삭제
    @Transactional
    public void removeArtwork(Long userId, Long collectionId, Long artworkId) {
        getOwnedCollection(userId, collectionId);
        CollectionArtwork ca = collectionArtworkRepository
                .findByCollectionIdAndArtworkId(collectionId, artworkId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ARTWORK_NOT_IN_COLLECTION));
        collectionArtworkRepository.delete(ca);
    }

    // 컬렉션 안 작품 목록
    @Transactional(readOnly = true)
    public List<ArtworkDetailResponse> getArtworksInCollection(Long userId, Long collectionId) {
        getOwnedCollection(userId, collectionId);
        return collectionArtworkRepository.findAllByCollectionId(collectionId)
                .stream()
                .map(ca -> ArtworkDetailResponse.from(ca.getArtwork()))
                .toList();
    }
    
    
  
}