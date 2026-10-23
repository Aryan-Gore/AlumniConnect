package com.alumniconnect.backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Either the person already has an account (status AUTHENTICATED, {@code auth}
 * filled in) or Google vouched for someone new and we still need their
 * enrollment number and department (status REGISTRATION_REQUIRED, with a
 * short-lived {@code registrationToken} to send back to /auth/google/register).
 */
@Getter
@Builder
@AllArgsConstructor
public class GoogleLoginResponse {

    public static final String AUTHENTICATED = "AUTHENTICATED";
    public static final String REGISTRATION_REQUIRED = "REGISTRATION_REQUIRED";

    private String status;
    private AuthResponse auth;
    private String registrationToken;
    private String email;
    private String fullName;
    private String pictureUrl;
}
