package com.assignment.urlshortener.util;

import java.security.SecureRandom;

public final class Base62CodeGenerator {

    private static final char[] ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();

    private final SecureRandom random;

    public Base62CodeGenerator() {
        this(new SecureRandom());
    }

    Base62CodeGenerator(SecureRandom random) {
        this.random = random;
    }

    public String generate(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("length must be positive");
        }

        StringBuilder value = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            value.append(ALPHABET[random.nextInt(ALPHABET.length)]);
        }
        return value.toString();
    }
}
