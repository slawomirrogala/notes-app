package com.betacom.betacom.exception;

public class ItemOrUserNotFoundException extends RuntimeException {
    public String getMessage() {
        return "Brak dostępu do tej notatki";
    }
}
