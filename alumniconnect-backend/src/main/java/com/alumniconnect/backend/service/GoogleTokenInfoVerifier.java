package com.alumniconnect.backend.service;

import com.alumniconnect.backend.exception.BadRequestException;
import com.alumniconnect.backend.security.GoogleIdentity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * Verifies the ID token by asking Google's own tokeninfo endpoint, which
 * checks the signature and expiry for us. We then check the claims that make
 * the token ours: audience (issued for THIS app), issuer, and a verified email.
 *
 * Trade-off, stated plainly: one HTTPS round-trip per sign-in is fine at
 * campus scale and needs no extra dependency. Google's docs recommend
 * verifying the signature locally against their published keys for
 * high-volume production use — implement GoogleTokenVerifier that way and
 * swap the bean if this ever matters.
 */
@Slf4j
@Service
public class GoogleTokenInfoVerifier implements GoogleTokenVerifier {

    private static final String TOKENINFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token={token}";

    private final String clientId;
    private final RestClient restClient;

    public GoogleTokenInfoVerifier(@Value("${app.google.client-id:}") String clientId) {
        this.clientId = clientId == null ? "" : clientId.trim();
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(5000);
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    @Override
    public boolean isConfigured() {
        return !clientId.isEmpty();
    }

    @Override
    public String clientId() {
        return clientId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public GoogleIdentity verify(String idToken) {
        if (!isConfigured()) {
            throw new BadRequestException("Google sign-in isn't set up on this server.");
        }
        if (idToken == null || idToken.isBlank()) {
            throw new BadRequestException("Google sign-in didn't return a credential. Please try again.");
        }

        Map<String, Object> claims;
        try {
            claims = restClient.get().uri(TOKENINFO_URL, idToken.trim()).retrieve().body(Map.class);
        } catch (RestClientException ex) {
            log.warn("Google token verification failed: {}", ex.getMessage());
            throw new BadRequestException("Google sign-in failed. Please try again.");
        }
        if (claims == null) {
            throw new BadRequestException("Google sign-in failed. Please try again.");
        }

        if (!clientId.equals(text(claims.get("aud")))) {
            throw new BadRequestException("That Google sign-in wasn't issued for AlumniConnect.");
        }
        String issuer = text(claims.get("iss"));
        if (!"accounts.google.com".equals(issuer) && !"https://accounts.google.com".equals(issuer)) {
            throw new BadRequestException("That Google sign-in came from an unexpected issuer.");
        }
        if (!"true".equalsIgnoreCase(text(claims.get("email_verified")))) {
            throw new BadRequestException("Your Google email address isn't verified.");
        }
        long expirySeconds;
        try {
            expirySeconds = Long.parseLong(text(claims.get("exp")));
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Google sign-in failed. Please try again.");
        }
        if (expirySeconds * 1000L < System.currentTimeMillis()) {
            throw new BadRequestException("That Google sign-in has expired. Please try again.");
        }

        String sub = text(claims.get("sub"));
        String email = text(claims.get("email"));
        if (sub == null || sub.isBlank() || email == null || email.isBlank()) {
            throw new BadRequestException("Google didn't share the details we need.");
        }

        String name = text(claims.get("name"));
        if (name == null || name.isBlank()) {
            name = email.substring(0, email.indexOf('@') > 0 ? email.indexOf('@') : email.length());
        }
        return new GoogleIdentity(sub, email.trim().toLowerCase(), name.trim(), text(claims.get("picture")));
    }

    private static String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
