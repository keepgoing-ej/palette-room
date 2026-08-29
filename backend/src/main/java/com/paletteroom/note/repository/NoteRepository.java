package com.paletteroom.note.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paletteroom.note.domain.Note;

public interface NoteRepository extends JpaRepository<Note, Long>{
	
	List<Note> findAllByUserId(Long userId);

}
