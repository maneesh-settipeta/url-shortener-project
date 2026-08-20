package com.assignment.urlshortener.service;

import com.assignment.urlshortener.model.ClickEvent;
import com.assignment.urlshortener.model.ShortUrl;
import com.assignment.urlshortener.repository.ClickEventRepository;
import com.assignment.urlshortener.util.PrivacyHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
public class AnalyticsRecorder {

    private static final Logger log =
            LoggerFactory.getLogger(AnalyticsRecorder.class);

    private final ClickEventRepository repository;
    private final Clock clock;

    @Autowired
    public AnalyticsRecorder(ClickEventRepository repository) {
        this(repository, Clock.systemUTC());
    }

    public AnalyticsRecorder(
            ClickEventRepository repository,
            Clock clock
    ) {
        this.repository = repository;
        this.clock = clock;
    }

    public void recordBestEffort(
            ShortUrl shortUrl,
            String referrer,
            String userAgent,
            String clientAddress
    ) {
        try {
            repository.saveAndFlush(
                    new ClickEvent(
                            shortUrl,
                            Instant.now(clock),
                            truncate(referrer, 512),
                            truncate(userAgent, 512),
                            PrivacyHash.sha256(clientAddress)
                    )
            );
        } catch (RuntimeException ex) {
            log.warn(
                    "Failed to persist click analytics for code={}",
                    shortUrl.getShortCode(),
                    ex
            );
        }
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }

        return value.length() <= max
                ? value
                : value.substring(0, max);
    }
}