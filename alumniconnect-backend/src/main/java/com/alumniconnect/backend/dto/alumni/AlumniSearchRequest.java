package com.alumniconnect.backend.dto.alumni;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Query-string filters for GET /alumni, bound by Spring from plain
 * parameters, e.g. {@code /alumni?query=priya&instituteId=1&skills=Java&skills=Spring&page=0&size=12}.
 * Every filter is optional; filters combine with AND.
 */
@Getter
@Setter
public class AlumniSearchRequest {
    /** Free text: matches name, company, job title or a skill. */
    private String query;
    private Long instituteId;
    private Long departmentId;
    private String batch;
    private Integer graduationYear;
    private String company;
    private String industry;
    private String location;
    /** Repeat the parameter to require several skills: skills=Java&amp;skills=Spring. */
    private List<String> skills;
    private Boolean openToMentoring;
    private Boolean availableForHiring;
    /** name (default) | year (newest graduates first) | recent (most recently joined first). */
    private String sort;
    private Integer page;
    private Integer size;
}
