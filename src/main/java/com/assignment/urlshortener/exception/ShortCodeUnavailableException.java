package com.assignment.urlshortener.exception;

public class ShortCodeUnavailableException extends RuntimeException {
    public ShortCodeUnavailableException(String message) {
        super(message);
    }
}
