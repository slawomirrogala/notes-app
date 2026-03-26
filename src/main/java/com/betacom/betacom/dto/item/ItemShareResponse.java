package com.betacom.betacom.dto.item;

import java.time.LocalDateTime;
import java.util.UUID;

public record ItemShareResponse(
        UUID itemId,
        UUID userId,
        String role,
        LocalDateTime grantedAt
) {}
