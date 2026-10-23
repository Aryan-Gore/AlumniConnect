package com.alumniconnect.backend.dto.alumni;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlumniVerifyRequest {
    @NotNull(message = "verified is required")
    private Boolean verified;
}
