package com.paletteroom.collection.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paletteroom.collection.domain.Collection;
import com.paletteroom.collection.dto.CollectionRequest;
import com.paletteroom.collection.dto.CollectionResponse;
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
}