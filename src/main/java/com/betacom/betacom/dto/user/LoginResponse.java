package com.betacom.betacom.dto.user;

public record LoginResponse(String token, long expiresIn) {
}