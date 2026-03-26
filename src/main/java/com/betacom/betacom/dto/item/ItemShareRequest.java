package com.betacom.betacom.dto.item;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ItemShareRequest(@NotNull UUID userId, @NotNull String role) {}
