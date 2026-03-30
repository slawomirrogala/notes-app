package com.betacom.betacom.dto.item;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ItemResponse(
        UUID id,
        String title,
        String content,
        Integer version,
        UUID ownerId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
