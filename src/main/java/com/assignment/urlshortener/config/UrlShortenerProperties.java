package com.assignment.urlshortener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.shortener")
public record UrlShortenerProperties(
        String baseUrl,
        int codeLength,
        int maxGenerationAttempts,
        Duration defaultTtl,
        Duration maxTtl,
        int createRequestsPerMinute
) {
    public UrlShortenerProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:8080";
        }
        if (codeLength < 6) {
            codeLength = 7;
        }
        if (maxGenerationAttempts < 1) {
            maxGenerationAttempts = 8;
        }
        if (defaultTtl == null || defaultTtl.isNegative() || defaultTtl.isZero()) {
            defaultTtl = Duration.ofDays(30);
        }
        if (maxTtl == null || maxTtl.compareTo(defaultTtl) < 0) {
            maxTtl = Duration.ofDays(365);
        }
        if (createRequestsPerMinute < 1) {
            createRequestsPerMinute = 30;
        }
        baseUrl = baseUrl.replaceAll("/+$", "");
    }
}
