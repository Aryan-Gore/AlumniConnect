package com.alumniconnect.backend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** Public, non-secret settings the sign-in page needs before anyone is logged in. */
@Getter
@Builder
@AllArgsConstructor
public class AuthConfigResponse {
    private boolean googleEnabled;
    /** The OAuth client id (public by design); empty when Google sign-in is off. */
    private String googleClientId;
}
