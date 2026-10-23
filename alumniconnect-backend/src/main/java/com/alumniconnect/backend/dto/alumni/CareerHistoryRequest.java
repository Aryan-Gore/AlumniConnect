package com.alumniconnect.backend.dto.alumni;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CareerHistoryRequest {

    @NotBlank(message = "Company is required")
    @Size(max = 150, message = "Company can be at most 150 characters")
    private String company;

    @NotBlank(message = "Job title is required")
    @Size(max = 150, message = "Job title can be at most 150 characters")
    private String jobTitle;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    /** Leave empty for a role you still hold. */
    private LocalDate endDate;

    /** Responsibilities and achievements, one bullet per line. */
    @Size(max = 4000, message = "Description can be at most 4000 characters")
    private String description;
}
