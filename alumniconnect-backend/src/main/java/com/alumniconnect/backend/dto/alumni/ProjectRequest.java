package com.alumniconnect.backend.dto.alumni;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class ProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(max = 200, message = "Name can be at most 200 characters")
    private String name;

    @Size(max = 4000, message = "Description can be at most 4000 characters")
    private String description;

    /** At most 20 entries of at most 50 characters each (the per-entry length is checked in the service). */
    @Size(max = 20, message = "List at most 20 technologies")
    private List<String> technologies;

    @Size(max = 255, message = "URL can be at most 255 characters")
    @Pattern(regexp = "^(|https?://\\S+)$", message = "Enter a full http(s) URL")
    private String projectUrl;

    @Size(max = 255, message = "URL can be at most 255 characters")
    @Pattern(regexp = "^(|https?://\\S+)$", message = "Enter a full http(s) URL")
    private String githubUrl;

    private LocalDate startDate;
    private LocalDate endDate;
}
