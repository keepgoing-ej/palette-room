package com.paletteroom.note.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paletteroom.note.domain.Note;

public interface NoteRepository extends JpaRepository<Note, Long>{

}
