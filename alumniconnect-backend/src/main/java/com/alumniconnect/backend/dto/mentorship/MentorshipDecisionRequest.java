package com.alumniconnect.backend.dto.mentorship;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MentorshipDecisionRequest {

    /** "ACCEPTED" or "REJECTED" — anything else is rejected as a bad request. */
    @NotNull(message = "decision is required")
    private String decision;
}
