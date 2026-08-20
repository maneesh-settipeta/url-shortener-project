package com.assignment.urlshortener.dto;

import java.time.Instant;
import java.util.List;

public record AnalyticsResponse(
        String code,
        long totalClicks,
        Instant firstClickAt,
        Instant lastClickAt,
        Instant from,
        Instant to,
        List<DailyClicks> clicksByDay
) {
}
