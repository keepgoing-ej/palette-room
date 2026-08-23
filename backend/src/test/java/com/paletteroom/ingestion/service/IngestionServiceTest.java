package com.paletteroom.ingestion.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class IngestionServiceTest {

    @Autowired
    IngestionService ingestionService;

    @Test
    void 소스당_5개씩_수집해본다() {
        ingestionService.ingestAll(5);
    }
}