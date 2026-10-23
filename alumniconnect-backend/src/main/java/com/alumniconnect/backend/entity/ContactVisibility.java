package com.alumniconnect.backend.entity;

/**
 * Per-field visibility for an alumnus's email and phone number.
 * PUBLIC = any signed-in user, ALUMNI_ONLY = other alumni (and admins),
 * PRIVATE = only the owner and admins.
 */
public enum ContactVisibility {
    PUBLIC,
    ALUMNI_ONLY,
    PRIVATE
}
