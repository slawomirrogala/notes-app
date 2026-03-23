package com.betacom.betacom.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UserRegistrationDto {
    private UUID id;
    private String login;
    private String password;
}
