package com.betacom.betacom.dto.user;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class UserRegistration {

    @NotBlank(message = "Login nie może być pusty")
    @Size(min = 3, max = 64, message = "Unikalny login, 3–64 znaki")
    private String login;
    @NotBlank(message = "Hasło nie może być puste")
    @Size(min = 8, message = "Hasło, minimum 8 znaków")
    private String password;
}
