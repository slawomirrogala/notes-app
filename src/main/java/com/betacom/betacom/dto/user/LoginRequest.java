package com.betacom.betacom.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Login jest wymagany")
    private String login;
    @NotBlank(message = "Hasło jest wymagane")
    private String password;
}