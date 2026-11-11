package com.academic.dashboard.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String identifier) {
        super(String.format("%s nao encontrado(a) para o identificador: %s", resourceName, identifier));
    }
}

