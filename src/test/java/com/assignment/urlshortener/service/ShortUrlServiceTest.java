package com.assignment.urlshortener.service;

import com.assignment.urlshortener.config.UrlShortenerProperties;
import com.assignment.urlshortener.dto.CreateShortUrlRequest;
import com.assignment.urlshortener.dto.ShortUrlResponse;
import com.assignment.urlshortener.exception.InvalidUrlException;
import com.assignment.urlshortener.exception.ShortCodeUnavailableException;
import com.assignment.urlshortener.model.ShortUrl;
import com.assignment.urlshortener.repository.ClickEventRepository;
import com.assignment.urlshortener.repository.ShortUrlRepository;
import com.assignment.urlshortener.util.Base62CodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShortUrlServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-20T14:00:00Z");

    private ShortUrlRepository shortUrlRepository;
    private ClickEventRepository clickEventRepository;
    private Base62CodeGenerator generator;
    private ShortUrlService service;

    @BeforeEach
    void setUp() {
        shortUrlRepository = mock(ShortUrlRepository.class);
        clickEventRepository = mock(ClickEventRepository.class);
        generator = mock(Base62CodeGenerator.class);

        UrlShortenerProperties properties = new UrlShortenerProperties(
                "http://localhost:8080",
                7,
                3,
                Duration.ofDays(30),
                Duration.ofDays(365),
                30
        );
        service = new ShortUrlService(
                shortUrlRepository,
                clickEventRepository,
                properties,
                generator,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void createsGeneratedShortUrl() {
        when(generator.generate(7)).thenReturn("Ab12Cd3");
        when(shortUrlRepository.existsByShortCode("Ab12Cd3")).thenReturn(false);
        when(shortUrlRepository.saveAndFlush(any(ShortUrl.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ShortUrlResponse response = service.create(new CreateShortUrlRequest(
                "https://example.com/path",
                null,
                null
        ));

        assertThat(response.code()).isEqualTo("Ab12Cd3");
        assertThat(response.shortUrl()).isEqualTo("http://localhost:8080/Ab12Cd3");
        assertThat(response.expiresAt()).isEqualTo(NOW.plus(Duration.ofDays(30)));
        assertThat(response.customAlias()).isFalse();
    }

    @Test
    void rejectsDuplicateCustomAlias() {
        when(shortUrlRepository.existsByShortCode("my-link")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateShortUrlRequest(
                "https://example.com",
                "my-link",
                null
        ))).isInstanceOf(ShortCodeUnavailableException.class);
    }

    @Test
    void rejectsExpirationBeyondMaximumTtl() {
        assertThatThrownBy(() -> service.create(new CreateShortUrlRequest(
                "https://example.com",
                null,
                NOW.plus(Duration.ofDays(366))
        ))).isInstanceOf(InvalidUrlException.class)
                .hasMessageContaining("maximum");
    }
}
