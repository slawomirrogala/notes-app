package com.betacom.betacom.dto.item;

import java.time.LocalDateTime;
import java.util.UUID;


public record ItemListResponse(
        UUID id,
        String title,
        String content,
        Integer version,
        UUID ownerId,
        String myRole,
        LocalDateTime updatedAt
){}

