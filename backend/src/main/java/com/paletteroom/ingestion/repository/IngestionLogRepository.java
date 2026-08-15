package com.paletteroom.ingestion.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.paletteroom.ingestion.domain.IngestionLog;

public interface IngestionLogRepository extends JpaRepository<IngestionLog, Long>{

}
