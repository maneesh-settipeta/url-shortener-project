package com.assignment.urlshortener.util;

import com.assignment.urlshortener.exception.InvalidUrlException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class UrlPolicy {

    private static final int MAX_URL_LENGTH = 2048;
    private static final Pattern CUSTOM_ALIAS = Pattern.compile("[A-Za-z0-9_-]{4,32}");
    private static final Set<String> RESERVED_ALIASES = Set.of(
            "api", "actuator", "health", "metrics", "favicon", "favicon.ico", "h2-console"
    );

    private UrlPolicy() {
    }

    public static String normalizeAndValidateUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            throw new InvalidUrlException("url is required");
        }

        String value = rawUrl.trim();
        if (value.length() > MAX_URL_LENGTH) {
            throw new InvalidUrlException("url must be at most 2048 characters");
        }

        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
                throw new InvalidUrlException("only http and https URLs are supported");
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new InvalidUrlException("url must include a valid host");
            }
            if (uri.getUserInfo() != null) {
                throw new InvalidUrlException("URLs containing embedded credentials are not allowed");
            }
            return uri.normalize().toString();
        } catch (URISyntaxException ex) {
            throw new InvalidUrlException("url is not a valid URI");
        }
    }

    public static String normalizeAndValidateAlias(String rawAlias) {
        if (rawAlias == null || rawAlias.isBlank()) {
            return null;
        }
        String alias = rawAlias.trim();
        if (!CUSTOM_ALIAS.matcher(alias).matches()) {
            throw new InvalidUrlException(
                    "customAlias must be 4-32 characters using letters, numbers, '-' or '_'"
            );
        }
        if (RESERVED_ALIASES.contains(alias.toLowerCase(Locale.ROOT))) {
            throw new InvalidUrlException("customAlias is reserved");
        }
        return alias;
    }
}
