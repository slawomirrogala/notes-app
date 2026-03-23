package com.betacom.betacom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class UserResponseDto {
    private UUID id;
    private String login;
    private LocalDateTime createdAt;
}
