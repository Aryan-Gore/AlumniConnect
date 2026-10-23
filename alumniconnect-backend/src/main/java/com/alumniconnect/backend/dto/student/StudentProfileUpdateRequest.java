package com.alumniconnect.backend.dto.student;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** PATCH semantics, like AlumniSelfUpdateRequest: omitted = unchanged, empty string = clear, skills replaces the list. */
@Getter
@Setter
public class StudentProfileUpdateRequest {

    @Size(max = 20, message = "Batch can be at most 20 characters")
    private String batch;

    @Size(max = 30, message = "List at most 30 skills")
    private List<String> skills;

    @Size(max = 2000, message = "Career interests can be at most 2000 characters")
    private String careerInterests;

    /** Fix a mistaken registration choice; the student's department group follows. */
    private Long departmentId;
}
