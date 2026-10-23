package com.alumniconnect.backend.dto.announcement;

import com.alumniconnect.backend.entity.Priority;
import com.alumniconnect.backend.entity.TargetAudience;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AnnouncementRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title can be at most 200 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(max = 8000, message = "Content can be at most 8000 characters")
    private String content;

    @NotNull(message = "Target audience is required")
    private TargetAudience targetAudience;

    /** Required when the audience is DEPARTMENT. */
    private Long targetDepartmentId;

    /** Required when the audience is BATCH. */
    @Size(max = 20, message = "Batch can be at most 20 characters")
    private String targetBatch;

    @NotNull(message = "Priority is required")
    private Priority priority;

    private LocalDate publishDate;
}
