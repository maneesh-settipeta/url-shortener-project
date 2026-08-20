package com.assignment.urlshortener.service;

import com.assignment.urlshortener.config.UrlShortenerProperties;
import com.assignment.urlshortener.exception.RateLimitExceededException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CreateRateLimiter {

    private static final long WINDOW_SECONDS = 60;

    private final int limit;
    private final Clock clock;
    private final ConcurrentMap<String, Window> windows =
            new ConcurrentHashMap<>();

    @Autowired
    public CreateRateLimiter(UrlShortenerProperties properties) {
        this(properties.createRequestsPerMinute(), Clock.systemUTC());
    }

    public CreateRateLimiter(int limit, Clock clock) {
        this.limit = limit;
        this.clock = clock;
    }

    public void check(String clientKey) {
        String key = (clientKey == null || clientKey.isBlank())
                ? "unknown"
                : clientKey;

        long windowStart =
                Instant.now(clock).getEpochSecond() / WINDOW_SECONDS;

        Window window = windows.compute(key, (ignored, existing) -> {
            if (existing == null ||
                    existing.windowStart() != windowStart) {

                return new Window(
                        windowStart,
                        new AtomicInteger(1)
                );
            }

            existing.counter().incrementAndGet();
            return existing;
        });

        if (window.counter().get() > limit) {
            throw new RateLimitExceededException(
                    "Create URL rate limit exceeded. Try again in the next minute."
            );
        }

        // Best-effort cleanup prevents unbounded growth
        // during a long-running demo.
        if (windows.size() > 10_000) {
            windows.entrySet().removeIf(
                    entry -> entry.getValue().windowStart()
                            < windowStart - 2
            );
        }
    }

    private record Window(
            long windowStart,
            AtomicInteger counter
    ) {
    }
}