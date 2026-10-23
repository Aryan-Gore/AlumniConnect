package com.alumniconnect.backend.entity;

public enum AuditAction {
    ADD_ALUMNI,
    UPDATE_ALUMNI,
    IMPORT_ALUMNI,
    EXPORT_ALUMNI,
    INVITE_ALUMNI,
    DEACTIVATE_ALUMNI,
    CREATE_EVENT,
    UPDATE_EVENT,
    APPROVE_JOB,
    REJECT_JOB,
    /** An admin removed a feed post or comment. */
    REMOVE_POST,
    VERIFY_ALUMNI,
    /** An admin removed a message from a department room (direct messages are never readable by admins). */
    REMOVE_MESSAGE,
    /** An admin switched any account (student, alumnus, admin) off. */
    DEACTIVATE_USER,
    REACTIVATE_USER
}
