package com.betacom.betacom.exception;

import java.util.UUID;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(UUID id) {
        super ("Nie znaleziono notatki o podanym id: " + id);
    }
}
