package com.betacom.betacom.dto.item;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ItemPatchRequest {

    private String title;
    private String content;

    @NotNull(message = "Wersja jest wymagana do edycji")
    private Integer version;
}
