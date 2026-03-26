package com.betacom.betacom.dto.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ItemRequest {

    @NotBlank(message = "Tytuł jest wymagany")
    @Size(min = 1, max = 255, message = "Tytuł musi mieć od 1 do 255 znaków")
    private String title;
    private String content;
}
