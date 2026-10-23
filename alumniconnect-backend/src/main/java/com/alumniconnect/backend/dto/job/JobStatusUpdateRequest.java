package com.alumniconnect.backend.dto.job;

import com.alumniconnect.backend.entity.JobStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobStatusUpdateRequest {
    @NotNull(message = "status is required")
    private JobStatus status;
}
