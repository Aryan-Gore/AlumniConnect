package com.alumniconnect.backend.entity;

public enum PostVisibility {
    /** Any signed-in user. */
    PUBLIC,
    /** Alumni and admins only. */
    ALUMNI_ONLY,
    /** People in the author's own department. */
    DEPARTMENT,
    /** People the author has a direct-message conversation with. */
    CONNECTIONS
}
