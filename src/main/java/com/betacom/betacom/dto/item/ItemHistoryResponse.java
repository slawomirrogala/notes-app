package com.betacom.betacom.dto.item;

import java.time.LocalDateTime;
import java.util.UUID;

public record ItemHistoryResponse(
        UUID id,
        String title,
        String content,
        Integer version,
        String changedBy,
        int revision,
        String revisionType,
        LocalDateTime timestamp
) {}
