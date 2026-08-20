package com.assignment.urlshortener.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "click_events", indexes = {
        @Index(name = "idx_click_events_url_time", columnList = "short_url_id,clicked_at")
})
public class ClickEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "short_url_id", nullable = false)
    private ShortUrl shortUrl;

    @Column(name = "clicked_at", nullable = false)
    private Instant clickedAt;

    @Column(name = "referrer", length = 512)
    private String referrer;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "client_hash", length = 64)
    private String clientHash;

    protected ClickEvent() {
    }

    public ClickEvent(ShortUrl shortUrl, Instant clickedAt, String referrer, String userAgent, String clientHash) {
        this.shortUrl = shortUrl;
        this.clickedAt = clickedAt;
        this.referrer = referrer;
        this.userAgent = userAgent;
        this.clientHash = clientHash;
    }

    public Long getId() { return id; }
    public ShortUrl getShortUrl() { return shortUrl; }
    public Instant getClickedAt() { return clickedAt; }
    public String getReferrer() { return referrer; }
    public String getUserAgent() { return userAgent; }
    public String getClientHash() { return clientHash; }
}
