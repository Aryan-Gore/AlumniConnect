package com.alumniconnect.backend.security;

import com.alumniconnect.backend.entity.User;

import java.security.Principal;
import java.time.Instant;

/**
 * Principal injected into every STOMP session by {@link WebSocketAuthInterceptor}.
 * The name is always the user id as a string (matches SimpMessagingTemplate's
 * convertAndSendToUser convention used by ChatPushService).
 */
public class StompPrincipal implements Principal {

    private final User user;
    private final Instant tokenExpiry;

    public StompPrincipal(User user, Instant tokenExpiry) {
        this.user = user;
        this.tokenExpiry = tokenExpiry;
    }

    /** Returns the user id as a string — used by Spring's user-destination resolution. */
    @Override
    public String getName() {
        return String.valueOf(user.getId());
    }

    public User getUser() {
        return user;
    }

    /** The JWT expiry remembered at CONNECT time; frames arriving after this are rejected. */
    public Instant getTokenExpiry() {
        return tokenExpiry;
    }
}
