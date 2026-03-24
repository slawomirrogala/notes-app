package com.betacom.betacom.dto;

public record LoginResponse(String token, long expiresIn) {
}