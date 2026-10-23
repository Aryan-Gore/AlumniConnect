package com.alumniconnect.backend.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Validates the JWT on every STOMP frame and sets a {@link StompPrincipal} as the session's
 * principal. Mirrors the logic of {@link JwtAuthFilter} but operates on STOMP headers.
 *
 * <ul>
 *   <li>CONNECT: extract Bearer token from the {@code Authorization} header, validate it,
 *       build a StompPrincipal, and remember the token's expiry in the session attributes.</li>
 *   <li>Subsequent frames (SEND, SUBSCRIBE, …): verify the token has not expired yet
 *       (compares clock against the expiry stored at CONNECT time).</li>
 *   <li>Inactive / unauthenticated sessions: the message is rejected with a
 *       {@link org.springframework.messaging.MessageDeliveryException}.</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final String SESSION_EXPIRY_ATTR = "tokenExpiry";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) return message;

        StompCommand command = accessor.getCommand();
        if (command == null) return message;

        if (StompCommand.CONNECT == command) {
            // --- Authenticate at CONNECT time ---
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new org.springframework.messaging.MessageDeliveryException(message,
                        "Missing or malformed Authorization header.");
            }
            String token = authHeader.substring(7);
            try {
                String email = jwtService.extractEmail(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                if (!jwtService.isTokenValid(token, userDetails)) {
                    throw new org.springframework.messaging.MessageDeliveryException(message,
                            "Invalid or expired token.");
                }
                CustomUserDetails cud = (CustomUserDetails) userDetails;
                Instant expiry = jwtService.extractExpiration(token).toInstant();

                StompPrincipal principal = new StompPrincipal(cud.getUser(), expiry);
                accessor.setUser(principal);

                // Persist expiry in session attributes for later frames
                if (accessor.getSessionAttributes() != null) {
                    accessor.getSessionAttributes().put(SESSION_EXPIRY_ATTR, expiry);
                }
            } catch (org.springframework.messaging.MessageDeliveryException mde) {
                throw mde;
            } catch (Exception ex) {
                throw new org.springframework.messaging.MessageDeliveryException(message,
                        "Authentication failed: " + ex.getMessage(), ex);
            }

        } else if (StompCommand.SEND == command
                || StompCommand.SUBSCRIBE == command
                || StompCommand.UNSUBSCRIBE == command) {
            // --- Reject frames after token expiry ---
            java.util.Map<String, Object> attrs = accessor.getSessionAttributes();
            if (attrs == null) {
                throw new org.springframework.messaging.MessageDeliveryException(message,
                        "No authenticated session.");
            }
            Instant expiry = (Instant) attrs.get(SESSION_EXPIRY_ATTR);
            if (expiry == null || Instant.now().isAfter(expiry)) {
                throw new org.springframework.messaging.MessageDeliveryException(message,
                        "Token has expired. Please reconnect.");
            }
        }

        return message;
    }
}
