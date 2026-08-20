package com.assignment.urlshortener.dto;

import java.time.LocalDate;

public record DailyClicks(LocalDate date, long clicks) {
}
