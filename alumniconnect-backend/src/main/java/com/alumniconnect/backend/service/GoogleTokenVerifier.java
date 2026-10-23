package com.alumniconnect.backend.service;

import com.alumniconnect.backend.security.GoogleIdentity;

/**
 * Checks a Google ID token ("credential") that the browser obtained from the
 * Google Identity Services button. Behind an interface so tests can fake it
 * and so the verification strategy can change without touching AuthService.
 */
public interface GoogleTokenVerifier {

    /** True when a Google OAuth client id has been configured (app.google.client-id). */
    boolean isConfigured();

    /** The configured client id, shown to the frontend so it can render the button. */
    String clientId();

    /**
     * @return the verified identity
     * @throws com.alumniconnect.backend.exception.BadRequestException if the token is invalid,
     *         expired, issued for another app, or the Google email is unverified
     */
    GoogleIdentity verify(String idToken);
}
