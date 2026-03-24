package com.betacom.betacom.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDto {

    @NotBlank(message = "Login jest wymagany")
    private String login;
    @NotBlank(message = "Hasło jest wymagane")
    private String password;
}