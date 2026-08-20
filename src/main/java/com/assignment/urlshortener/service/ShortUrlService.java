package com.assignment.urlshortener.service;

import com.assignment.urlshortener.config.UrlShortenerProperties;
import com.assignment.urlshortener.dto.AnalyticsResponse;
import com.assignment.urlshortener.dto.CreateShortUrlRequest;
import com.assignment.urlshortener.dto.DailyClicks;
import com.assignment.urlshortener.dto.ShortUrlResponse;
import com.assignment.urlshortener.exception.InvalidUrlException;
import com.assignment.urlshortener.exception.ShortCodeNotFoundException;
import com.assignment.urlshortener.exception.ShortCodeUnavailableException;
import com.assignment.urlshortener.exception.ShortUrlGoneException;
import com.assignment.urlshortener.model.ClickEvent;
import com.assignment.urlshortener.model.ShortUrl;
import com.assignment.urlshortener.repository.ClickEventRepository;
import com.assignment.urlshortener.repository.ShortUrlRepository;
import com.assignment.urlshortener.util.Base62CodeGenerator;
import com.assignment.urlshortener.util.UrlPolicy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ShortUrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final ClickEventRepository clickEventRepository;
    private final UrlShortenerProperties properties;
    private final Base62CodeGenerator codeGenerator;
    private final Clock clock;

@Autowired
public ShortUrlService(
        ShortUrlRepository shortUrlRepository,
        ClickEventRepository clickEventRepository,
        UrlShortenerProperties properties
) {
        this(shortUrlRepository, clickEventRepository, properties, new Base62CodeGenerator(), Clock.systemUTC());
    }

    ShortUrlService(
            ShortUrlRepository shortUrlRepository,
            ClickEventRepository clickEventRepository,
            UrlShortenerProperties properties,
            Base62CodeGenerator codeGenerator,
            Clock clock
    ) {
        this.shortUrlRepository = shortUrlRepository;
        this.clickEventRepository = clickEventRepository;
        this.properties = properties;
        this.codeGenerator = codeGenerator;
        this.clock = clock;
    }

    public ShortUrlResponse create(CreateShortUrlRequest request) {
        Instant now = Instant.now(clock);
        String originalUrl = UrlPolicy.normalizeAndValidateUrl(request.url());
        String customAlias = UrlPolicy.normalizeAndValidateAlias(request.customAlias());
        Instant expiresAt = resolveExpiration(request.expiresAt(), now);

        ShortUrl shortUrl = customAlias != null
                ? createWithCustomAlias(customAlias, originalUrl, now, expiresAt)
                : createWithGeneratedCode(originalUrl, now, expiresAt);

        return toResponse(shortUrl, 0L);
    }

    @Transactional(readOnly = true)
    public ShortUrl getUsableByCode(String code) {
        ShortUrl shortUrl = find(code);
        if (!shortUrl.isActive() || shortUrl.isExpired(Instant.now(clock))) {
            throw new ShortUrlGoneException(code);
        }
        return shortUrl;
    }

    @Transactional(readOnly = true)
    public ShortUrlResponse getMetadata(String code) {
        ShortUrl shortUrl = find(code);
        long totalClicks = clickEventRepository.countByShortUrl_Id(shortUrl.getId());
        return toResponse(shortUrl, totalClicks);
    }

    @Transactional
    public void deactivate(String code) {
        ShortUrl shortUrl = find(code);
        shortUrl.deactivate();
    }

    @Transactional(readOnly = true)
    public AnalyticsResponse analytics(String code, Instant from, Instant to) {
        ShortUrl shortUrl = find(code);
        Instant effectiveTo = to == null ? Instant.now(clock) : to;
        Instant effectiveFrom = from == null ? shortUrl.getCreatedAt() : from;

        if (effectiveFrom.isAfter(effectiveTo)) {
            throw new InvalidUrlException("analytics 'from' must be before or equal to 'to'");
        }

        List<ClickEvent> events = clickEventRepository
                .findByShortUrl_IdAndClickedAtBetweenOrderByClickedAtAsc(
                        shortUrl.getId(), effectiveFrom, effectiveTo
                );

        Map<LocalDate, Long> grouped = new LinkedHashMap<>();
        for (ClickEvent event : events) {
            LocalDate day = event.getClickedAt().atZone(ZoneOffset.UTC).toLocalDate();
            grouped.merge(day, 1L, Long::sum);
        }

        List<DailyClicks> clicksByDay = new ArrayList<>();
        grouped.forEach((day, count) -> clicksByDay.add(new DailyClicks(day, count)));

        Instant first = events.isEmpty() ? null : events.getFirst().getClickedAt();
        Instant last = events.isEmpty() ? null : events.getLast().getClickedAt();

        return new AnalyticsResponse(
                code,
                events.size(),
                first,
                last,
                effectiveFrom,
                effectiveTo,
                List.copyOf(clicksByDay)
        );
    }

    private ShortUrl createWithCustomAlias(
            String customAlias,
            String originalUrl,
            Instant now,
            Instant expiresAt
    ) {
        if (shortUrlRepository.existsByShortCode(customAlias)) {
            throw new ShortCodeUnavailableException("customAlias is already in use");
        }

        try {
            return shortUrlRepository.saveAndFlush(
                    new ShortUrl(customAlias, originalUrl, now, expiresAt, true)
            );
        } catch (DataIntegrityViolationException ex) {
            // Handles a race where another request claims the alias after the exists check.
            throw new ShortCodeUnavailableException("customAlias is already in use");
        }
    }

    private ShortUrl createWithGeneratedCode(String originalUrl, Instant now, Instant expiresAt) {
        for (int attempt = 0; attempt < properties.maxGenerationAttempts(); attempt++) {
            String code = codeGenerator.generate(properties.codeLength());
            if (shortUrlRepository.existsByShortCode(code)) {
                continue;
            }

            try {
                return shortUrlRepository.saveAndFlush(
                        new ShortUrl(code, originalUrl, now, expiresAt, false)
                );
            } catch (DataIntegrityViolationException ignored) {
                // Extremely rare collision between the existence check and insert; retry.
            }
        }
        throw new ShortCodeUnavailableException("could not allocate a unique short code; retry the request");
    }

    private Instant resolveExpiration(Instant requested, Instant now) {
        Instant maximum = now.plus(properties.maxTtl());
        Instant expiresAt = requested == null ? now.plus(properties.defaultTtl()) : requested;

        if (!expiresAt.isAfter(now)) {
            throw new InvalidUrlException("expiresAt must be in the future");
        }
        if (expiresAt.isAfter(maximum)) {
            throw new InvalidUrlException("expiresAt exceeds the maximum allowed TTL");
        }
        return expiresAt;
    }

    private ShortUrl find(String code) {
        return shortUrlRepository.findByShortCode(code)
                .orElseThrow(() -> new ShortCodeNotFoundException(code));
    }

    private ShortUrlResponse toResponse(ShortUrl entity, long totalClicks) {
        return new ShortUrlResponse(
                entity.getShortCode(),
                properties.baseUrl() + "/" + entity.getShortCode(),
                entity.getOriginalUrl(),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.isActive() && !entity.isExpired(Instant.now(clock)),
                entity.isCustomAlias(),
                totalClicks
        );
    }
}
