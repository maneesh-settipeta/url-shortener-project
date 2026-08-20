package com.assignment.urlshortener.controller;

import com.assignment.urlshortener.model.ShortUrl;
import com.assignment.urlshortener.service.AnalyticsRecorder;
import com.assignment.urlshortener.service.ShortUrlService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class RedirectController {

    private final ShortUrlService service;
    private final AnalyticsRecorder analyticsRecorder;

    public RedirectController(ShortUrlService service, AnalyticsRecorder analyticsRecorder) {
        this.service = service;
        this.analyticsRecorder = analyticsRecorder;
    }

    @GetMapping("/{code:[A-Za-z0-9_-]+}")
    public ResponseEntity<Void> redirect(@PathVariable String code, HttpServletRequest request) {
        ShortUrl shortUrl = service.getUsableByCode(code);

        analyticsRecorder.recordBestEffort(
                shortUrl,
                request.getHeader("Referer"),
                request.getHeader("User-Agent"),
                request.getRemoteAddr()
        );

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(shortUrl.getOriginalUrl()))
                .build();
    }
}
