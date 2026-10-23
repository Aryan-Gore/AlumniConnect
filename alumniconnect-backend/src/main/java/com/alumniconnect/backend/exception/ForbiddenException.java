package com.alumniconnect.backend.exception;

/**
 * The caller is authenticated but not allowed to do this particular thing
 * (e.g. posting to another department's group). Distinct from
 * ResourceNotFoundException, which is used when we'd rather not reveal that
 * the resource exists at all.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
