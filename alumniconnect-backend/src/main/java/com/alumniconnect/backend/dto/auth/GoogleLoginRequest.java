package com.alumniconnect.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleLoginRequest {
    /** The ID token (JWT) Google Identity Services hands the browser as "credential". */
    @NotBlank(message = "Google credential is required")
    private String credential;
}
