package com.paletteroom.note.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paletteroom.artwork.domain.Artwork;
import com.paletteroom.artwork.repository.ArtworkRepository;
import com.paletteroom.common.exception.BusinessException;
import com.paletteroom.common.exception.ErrorCode;
import com.paletteroom.note.domain.Note;
import com.paletteroom.note.dto.NoteRequest;
import com.paletteroom.note.dto.NoteResponse;
import com.paletteroom.note.repository.NoteRepository;
import com.paletteroom.user.domain.User;
import com.paletteroom.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final ArtworkRepository artworkRepository;

    @Transactional
    public NoteResponse create(Long userId, NoteRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // ★ nullable artwork: 없으면 자유 노트, 있으면 존재 검증
        Artwork artwork = (request.artworkId() == null) ? null
                : artworkRepository.findById(request.artworkId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.ARTWORK_NOT_FOUND));

        Note note = noteRepository.save(Note.builder()
                .user(user)
                .artwork(artwork)
                .content(request.content())
                .moodColor(request.moodColor())
                .build());
        return NoteResponse.from(note);
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> getMyNotes(Long userId) {
        return noteRepository.findAllByUserId(userId)
                .stream()
                .map(NoteResponse::from)
                .toList();
    }

    @Transactional
    public NoteResponse update(Long userId, Long noteId, NoteRequest request) {
        Note note = getOwnedNote(userId, noteId);
        note.update(request.content(), request.moodColor());   // 더티 체킹
        return NoteResponse.from(note);
    }

    @Transactional
    public void delete(Long userId, Long noteId) {
        Note note = getOwnedNote(userId, noteId);
        noteRepository.delete(note);
    }

    private Note getOwnedNote(Long userId, Long noteId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTE_NOT_FOUND));
        if (!note.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOTE_ACCESS_DENIED);
        }
        return note;
    }
}