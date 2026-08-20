package com.assignment.urlshortener.dto;

import java.time.Instant;

public record ShortUrlResponse(
        String code,
        String shortUrl,
        String originalUrl,
        Instant createdAt,
        Instant expiresAt,
        boolean active,
        boolean customAlias,
        long totalClicks
) {
}
