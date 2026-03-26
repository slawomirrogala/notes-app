package com.betacom.betacom.exception;

public class ItemOrUserNotFoundException extends RuntimeException {
    public String getMessage() {
        return "Notatka lub użytkownik nie istnieje";
    }
}
