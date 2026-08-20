package com.assignment.urlshortener.controller;

import com.assignment.urlshortener.repository.ClickEventRepository;
import com.assignment.urlshortener.repository.ShortUrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UrlShortenerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShortUrlRepository shortUrlRepository;

    @Autowired
    private ClickEventRepository clickEventRepository;

    @BeforeEach
    void cleanDatabase() {
        clickEventRepository.deleteAll();
        shortUrlRepository.deleteAll();
    }

    @Test
    void endToEndCreateRedirectAnalyticsAndDeactivate() throws Exception {
        String createJson = """
                {
                  "url": "https://example.com/docs",
                  "customAlias": "vendor-demo"
                }
                """;

        mockMvc.perform(post("/api/v1/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("X-Request-Id"))
                .andExpect(jsonPath("$.code").value("vendor-demo"))
                .andExpect(jsonPath("$.shortUrl").value("http://localhost:8080/vendor-demo"));

        mockMvc.perform(get("/vendor-demo")
                        .header("User-Agent", "integration-test"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "https://example.com/docs"));

        mockMvc.perform(get("/api/v1/urls/vendor-demo/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("vendor-demo"))
                .andExpect(jsonPath("$.totalClicks", greaterThanOrEqualTo(1)));

        mockMvc.perform(delete("/api/v1/urls/vendor-demo"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/vendor-demo"))
                .andExpect(status().isGone());
    }

    @Test
    void rejectsInvalidUrl() throws Exception {
        mockMvc.perform(post("/api/v1/urls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"javascript:alert(1)\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("only http and https URLs are supported"));
    }
}
