package com.betacom.betacom.exception;

import lombok.Getter;

@Getter
public class VersionConflictException extends RuntimeException {

    private final Integer version;

    public VersionConflictException(Integer version) {
        super("Konflikt wersji. Aktualna wartość version wynosi: " + version);
        this.version = version;
    }
}
