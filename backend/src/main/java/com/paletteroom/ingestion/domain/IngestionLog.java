package com.paletteroom.ingestion.domain;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="ingestion_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class IngestionLog {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; 
	
	@Column(nullable = false, length = 20)
	private String source;
	
	@Column(name = "started_at", nullable = false, updatable = false)
	private LocalDateTime startedAt;
	
	@Column(name = "finished_at")
	private LocalDateTime finishedAt;
	
	@Column(nullable = false, length = 20)
	private String status;
	
	@Column(nullable = false)
	private int fetchedCount;
	
	@Column(nullable = false)
	private int savedCount;
	
	@Column(nullable = false)
	private int skippedCount;
	
	@Column(nullable = false)
	private int failedCount;
	
	@Column(length = 1000)
	private String errorMessage;

	@Builder
	public IngestionLog(String source, LocalDateTime startedAt) {
	    this.source = source;
	    this.startedAt = startedAt;
	    this.status = "RUNNING";   // 고정
	}

	// 성공
	public void success(int fetched, int saved, int skipped, int failed) {
	    this.status = "SUCCESS";
	    this.finishedAt = LocalDateTime.now();
	    this.fetchedCount = fetched;
	    this.savedCount = saved;
	    this.skippedCount = skipped;
	    this.failedCount = failed;
	}

	// 실패
	public void fail(String errorMessage) {
	    this.status = "FAILED";
	    this.finishedAt = LocalDateTime.now();
	    this.errorMessage = errorMessage;
	}
}
