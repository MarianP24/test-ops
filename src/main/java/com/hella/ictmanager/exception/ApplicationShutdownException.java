package com.hella.ictmanager.exception;

public class ApplicationShutdownException extends RuntimeException {
    public ApplicationShutdownException(String message, Throwable cause) {
        super(message, cause);
    }
}
