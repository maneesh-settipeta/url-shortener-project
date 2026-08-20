package com.assignment.urlshortener.repository;

import com.assignment.urlshortener.model.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {
    long countByShortUrl_Id(Long shortUrlId);

    List<ClickEvent> findByShortUrl_IdAndClickedAtBetweenOrderByClickedAtAsc(
            Long shortUrlId,
            Instant from,
            Instant to
    );
}
