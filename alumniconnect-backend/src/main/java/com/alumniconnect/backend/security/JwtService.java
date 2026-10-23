package com.alumniconnect.backend.security;

import com.alumniconnect.backend.exception.BadRequestException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMillis;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        // HS256 needs a >= 256-bit key. The default in application.yml is a
        // placeholder — generate a real one (openssl rand -base64 64) before
        // this ever touches a real deployment.
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationMinutes * 60 * 1000;
    }

    public String generateToken(CustomUserDetails userDetails) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("userId", userDetails.getId())
                .claim("role", userDetails.getRole().name())
                .claim("fullName", userDetails.getFullName())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    // ------------------------------------------------------------------
    // Google sign-up hand-off: between "Google says this person is new" and
    // "they told us their enrollment number" we must remember who Google
    // vouched for, without trusting the browser. A short-lived signed token
    // does that. Its subject is "google:<sub>", which no user email can ever
    // equal, so it can't be replayed as an ordinary bearer token.
    // ------------------------------------------------------------------

    private static final String PURPOSE_CLAIM = "purpose";
    private static final String GOOGLE_REGISTER_PURPOSE = "google-register";
    private static final String GOOGLE_SUBJECT_PREFIX = "google:";
    private static final long GOOGLE_REGISTRATION_MINUTES = 15;

    public String generateGoogleRegistrationToken(GoogleIdentity identity) {
        Date now = new Date();
        return Jwts.builder()
                .subject(GOOGLE_SUBJECT_PREFIX + identity.getSub())
                .claim(PURPOSE_CLAIM, GOOGLE_REGISTER_PURPOSE)
                .claim("email", identity.getEmail())
                .claim("name", identity.getFullName())
                .claim("picture", identity.getPictureUrl())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + GOOGLE_REGISTRATION_MINUTES * 60 * 1000))
                .signWith(signingKey)
                .compact();
    }

    public GoogleIdentity parseGoogleRegistrationToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String subject = claims.getSubject();
            if (!GOOGLE_REGISTER_PURPOSE.equals(claims.get(PURPOSE_CLAIM, String.class))
                    || subject == null || !subject.startsWith(GOOGLE_SUBJECT_PREFIX)) {
                throw new BadRequestException("That sign-up session isn't valid. Start again with Google.");
            }
            return new GoogleIdentity(
                    subject.substring(GOOGLE_SUBJECT_PREFIX.length()),
                    claims.get("email", String.class),
                    claims.get("name", String.class),
                    claims.get("picture", String.class));
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BadRequestException("That sign-up session has expired. Start again with Google.");
        }
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** When the given (signed) token stops being valid. Throws a JwtException for an invalid token. */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String email = extractEmail(token);
            return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }
}
