package com.alumniconnect.backend.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** What we trust about a person after Google has vouched for them. */
@Getter
@AllArgsConstructor
public class GoogleIdentity {
    /** Google's stable account id — the key we link on (emails can change, sub cannot). */
    private final String sub;
    private final String email;
    private final String fullName;
    private final String pictureUrl;
}
