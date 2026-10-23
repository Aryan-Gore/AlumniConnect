package com.alumniconnect.backend.dto.job;

import com.alumniconnect.backend.entity.JobType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class JobPostingRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Company is required")
    private String company;

    private String location;

    @NotNull(message = "Job type is required")
    private JobType jobType;

    private String description;

    private LocalDate expiresAt;
}
