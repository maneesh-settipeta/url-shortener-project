package com.assignment.urlshortener.util;

import com.assignment.urlshortener.exception.InvalidUrlException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrlPolicyTest {

    @Test
    void acceptsHttpAndHttpsUrls() {
        assertThat(UrlPolicy.normalizeAndValidateUrl(" https://example.com/a/../b "))
                .isEqualTo("https://example.com/b");
        assertThat(UrlPolicy.normalizeAndValidateUrl("http://example.org"))
                .isEqualTo("http://example.org");
    }

    @Test
    void rejectsUnsafeSchemesAndEmbeddedCredentials() {
        assertThatThrownBy(() -> UrlPolicy.normalizeAndValidateUrl("javascript:alert(1)"))
                .isInstanceOf(InvalidUrlException.class);
        assertThatThrownBy(() -> UrlPolicy.normalizeAndValidateUrl("https://user:pass@example.com"))
                .isInstanceOf(InvalidUrlException.class);
    }

    @Test
    void validatesCustomAlias() {
        assertThat(UrlPolicy.normalizeAndValidateAlias("My_Link-01"))
                .isEqualTo("My_Link-01");
        assertThatThrownBy(() -> UrlPolicy.normalizeAndValidateAlias("api"))
                .isInstanceOf(InvalidUrlException.class);
        assertThatThrownBy(() -> UrlPolicy.normalizeAndValidateAlias("bad alias"))
                .isInstanceOf(InvalidUrlException.class);
    }
}
