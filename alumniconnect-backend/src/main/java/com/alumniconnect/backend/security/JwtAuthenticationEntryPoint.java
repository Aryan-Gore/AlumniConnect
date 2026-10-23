package com.alumniconnect.backend.security;

import com.alumniconnect.backend.dto.common.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * Without this bean, Spring Security's fallback for an unconfigured
 * exceptionHandling setup is Http403ForbiddenEntryPoint — every
 * unauthenticated request gets a bare 403, indistinguishable from a
 * genuine role-based access denial. That's not just a wrong status code:
 * the frontend's axios interceptor (axiosClient.js) specifically checks
 * for 401 to clear the stored token and redirect to /login. Under a 403,
 * a user with a missing or expired token doesn't get bounced to login —
 * they just see broken pages, because the code path that would fix it
 * never fires.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // Spring's own ObjectMapper bean: a bare `new ObjectMapper()` cannot
    // serialise the Instant timestamp in ErrorResponse (no JavaTimeModule).
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("Authentication required.")
                .path(request.getRequestURI())
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
