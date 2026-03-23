package com.betacom.betacom.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public String getMessage() {
        return "Login jest już zajęty";
    }
}
