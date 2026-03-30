package com.betacom.betacom.dto.user;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record UserResponse(
        UUID id,
        String login,
        LocalDateTime createdAt
) {}
