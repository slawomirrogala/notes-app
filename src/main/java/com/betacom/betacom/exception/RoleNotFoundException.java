package com.betacom.betacom.exception;

public class RoleNotFoundException extends RuntimeException {
    public String getMessage() {
        return "Nieprawidłowa rola lub brakujące pola";
    }
}
