package com.assignment.urlshortener.exception;

public class ShortUrlGoneException extends RuntimeException {
    public ShortUrlGoneException(String code) {
        super("Short URL is inactive or expired: " + code);
    }
}
