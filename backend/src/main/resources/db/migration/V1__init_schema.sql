-- =====================================================================
-- Palette Room - Initial Schema
-- Version : V1
-- Date    : 2026-08-10
-- DBMS    : MySQL 8.0
-- Ref     : 04_db_design_palette_room.md
-- Path    : src/main/resources/db/migration/V1__init_schema.sql
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. users : 회원
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    email       VARCHAR(100) NOT NULL                COMMENT '로그인 ID',
    password    VARCHAR(60)  NOT NULL                COMMENT 'BCrypt 해시(60자 고정)',
    nickname    VARCHAR(20)  NOT NULL                COMMENT '2~20자',
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '회원';


-- ---------------------------------------------------------------------
-- 2. refresh_tokens : 리프레시 토큰
--    원문이 아닌 SHA-256 해시를 저장한다 (ADR-16)
-- ---------------------------------------------------------------------
CREATE TABLE refresh_tokens (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    user_id     BIGINT      NOT NULL,
    token_hash  CHAR(64)    NOT NULL                COMMENT 'SHA-256 hex',
    expires_at  DATETIME(6) NOT NULL                COMMENT '발급 + 14일',
    created_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_token_hash (token_hash),
    KEY idx_refresh_user (user_id),
    CONSTRAINT fk_refresh_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '리프레시 토큰';


-- ---------------------------------------------------------------------
-- 3. artworks : 작품
--    (source, source_id) 복합 UNIQUE 가 중복 수집 방지의 핵심
-- ---------------------------------------------------------------------
CREATE TABLE artworks (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    source        VARCHAR(20)   NOT NULL              COMMENT 'MET / CHICAGO / RIJKS / CLEVELAND',
    source_id     VARCHAR(50)   NOT NULL              COMMENT '미술관 원본 ID',
    title         VARCHAR(500)  NOT NULL,
    artist        VARCHAR(255)  NULL                  COMMENT '작가 미상 다수 존재',
    date_display  VARCHAR(100)  NULL                  COMMENT '"c. 1503-1519" 등 원문 표기',
    medium        VARCHAR(255)  NULL,
    department    VARCHAR(100)  NULL,
    image_url     VARCHAR(1000) NOT NULL              COMMENT '미술관 원본 이미지 URL',
    source_url    VARCHAR(1000) NOT NULL              COMMENT '링크백 대상(NFR-06)',
    license       VARCHAR(50)   NOT NULL              COMMENT 'CC0 / Public Domain',
    created_at    DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_artwork_source (source, source_id),
    KEY idx_artwork_created (created_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '미술 작품';


-- ---------------------------------------------------------------------
-- 4. artwork_colors : 작품 지배 색상 (작품당 5행)
--    idx_lab_search 가 이 프로젝트 성능의 핵심
--    'rank' 는 MySQL 8 예약어이므로 color_rank 사용 (ADR-11)
--    LAB 은 부동소수 오차 회피를 위해 DECIMAL (ADR-12)
-- ---------------------------------------------------------------------
CREATE TABLE artwork_colors (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    artwork_id   BIGINT        NOT NULL,
    color_rank   TINYINT       NOT NULL              COMMENT '1~5, 점유율 순위',
    hex          CHAR(7)       NOT NULL              COMMENT '#RRGGBB',
    lab_l        DECIMAL(6, 2) NOT NULL              COMMENT '명도 0~100',
    lab_a        DECIMAL(6, 2) NOT NULL              COMMENT '녹-적 -128~127',
    lab_b        DECIMAL(6, 2) NOT NULL              COMMENT '청-황 -128~127',
    color_ratio  DECIMAL(5, 4) NOT NULL              COMMENT '점유 비율 0.0000~1.0000',
    created_at   DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_artwork_color_rank (artwork_id, color_rank),
    KEY idx_lab_search (lab_l, lab_a, lab_b, artwork_id, color_ratio),
    CONSTRAINT fk_color_artwork FOREIGN KEY (artwork_id)
        REFERENCES artworks (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '작품 지배 색상';


-- ---------------------------------------------------------------------
-- 5. collections : 컬렉션(나만의 전시실)
--    사용자당 20개 제한은 Service 레이어에서 검증 (DB 표현 불가)
-- ---------------------------------------------------------------------
CREATE TABLE collections (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    user_id      BIGINT      NOT NULL,
    name         VARCHAR(50) NOT NULL                COMMENT '1~50자, 이모지 허용',
    theme_color  CHAR(7)     NOT NULL                COMMENT '대표 색 #RRGGBB',
    created_at   DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at   DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_collection_user (user_id, created_at),
    CONSTRAINT fk_collection_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '컬렉션';


-- ---------------------------------------------------------------------
-- 6. collection_artworks : 컬렉션-작품 교차 테이블
--    uk_collection_artwork 가 409(중복 저장) 응답의 최종 근거
-- ---------------------------------------------------------------------
CREATE TABLE collection_artworks (
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    collection_id  BIGINT      NOT NULL,
    artwork_id     BIGINT      NOT NULL,
    added_at       DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_collection_artwork (collection_id, artwork_id),
    KEY idx_ca_artwork (artwork_id),
    CONSTRAINT fk_ca_collection FOREIGN KEY (collection_id)
        REFERENCES collections (id) ON DELETE CASCADE,
    CONSTRAINT fk_ca_artwork FOREIGN KEY (artwork_id)
        REFERENCES artworks (id) ON DELETE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '컬렉션에 담긴 작품';


-- ---------------------------------------------------------------------
-- 7. notes : 노트
--    artwork_id NULL = 자유 노트 (FR-04-02)
-- ---------------------------------------------------------------------
CREATE TABLE notes (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    user_id     BIGINT      NOT NULL,
    artwork_id  BIGINT      NULL                     COMMENT 'NULL 이면 자유 노트',
    content     TEXT        NOT NULL                 COMMENT '1~5000자 마크다운',
    mood_color  CHAR(7)     NULL                     COMMENT '무드 컬러 #RRGGBB',
    created_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    KEY idx_note_user (user_id, created_at),
    KEY idx_note_artwork (artwork_id),
    KEY idx_note_mood (user_id, mood_color),
    CONSTRAINT fk_note_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_note_artwork FOREIGN KEY (artwork_id)
        REFERENCES artworks (id) ON DELETE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '감상·창작 노트';


-- ---------------------------------------------------------------------
-- 8. ingestion_logs : 배치 실행 이력
--    status='RUNNING' 조회가 배치 중복 실행 방지 근거 (R-06)
-- ---------------------------------------------------------------------
CREATE TABLE ingestion_logs (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    source         VARCHAR(20)   NOT NULL,
    started_at     DATETIME(6)   NOT NULL,
    finished_at    DATETIME(6)   NULL                COMMENT '실행 중이면 NULL',
    status         VARCHAR(20)   NOT NULL            COMMENT 'RUNNING / SUCCESS / PARTIAL / FAILED',
    fetched_count  INT           NOT NULL DEFAULT 0,
    saved_count    INT           NOT NULL DEFAULT 0,
    skipped_count  INT           NOT NULL DEFAULT 0  COMMENT '중복으로 건너뜀',
    failed_count   INT           NOT NULL DEFAULT 0  COMMENT '이미지 실패 등',
    error_message  VARCHAR(1000) NULL,
    PRIMARY KEY (id),
    KEY idx_ingestion_source_time (source, started_at)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '미술관 수집 배치 이력';
