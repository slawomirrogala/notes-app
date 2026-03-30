package com.betacom.betacom.dto.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

@Data
public class UserRegistration {

    @NotBlank(message = "Login jest wymagany")
    @Length(min = 3, message = "Login jest za krótki. Minimalna długość 3 znaki")
    @Length(max = 64, message = "Login jest za długi. Maksymalna długość 64 znaki")
    private String login;

    @NotBlank(message = "Hasło jest wymagane")
    @Length(min = 8, message = "Hasło jest za słabe. Minimalna długość 8 znaków")
    private String password;
}
