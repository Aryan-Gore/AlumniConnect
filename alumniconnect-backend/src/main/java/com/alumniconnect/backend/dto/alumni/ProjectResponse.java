package com.alumniconnect.backend.dto.alumni;

import com.alumniconnect.backend.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private List<String> technologies;
    private String projectUrl;
    private String githubUrl;
    private LocalDate startDate;
    private LocalDate endDate;

    public static ProjectResponse from(Project p) {
        return ProjectResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .technologies(p.getTechnologies() == null ? List.of() : List.copyOf(p.getTechnologies()))
                .projectUrl(p.getProjectUrl())
                .githubUrl(p.getGithubUrl())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .build();
    }
}
