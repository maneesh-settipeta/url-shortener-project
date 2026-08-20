package com.assignment.urlshortener.service;

import com.assignment.urlshortener.exception.RateLimitExceededException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateRateLimiterTest {

    @Test
    void rejectsRequestsAboveLimitInSameWindow() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-20T14:00:00Z"), ZoneOffset.UTC);
        CreateRateLimiter limiter = new CreateRateLimiter(2, clock);

        limiter.check("127.0.0.1");
        limiter.check("127.0.0.1");

        assertThatThrownBy(() -> limiter.check("127.0.0.1"))
                .isInstanceOf(RateLimitExceededException.class);
    }
}
