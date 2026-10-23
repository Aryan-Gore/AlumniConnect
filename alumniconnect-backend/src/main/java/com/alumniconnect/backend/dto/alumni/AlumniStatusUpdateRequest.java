package com.alumniconnect.backend.dto.alumni;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlumniStatusUpdateRequest {
    @NotNull(message = "isActive is required")
    private Boolean isActive;
}
