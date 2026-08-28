package com.paletteroom.collection.dto;

import java.util.List;

public record BulkAddRequest(List<Long> artworkIds) {
}