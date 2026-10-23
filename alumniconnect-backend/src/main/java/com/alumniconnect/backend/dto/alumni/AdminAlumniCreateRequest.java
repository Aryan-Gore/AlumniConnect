package com.alumniconnect.backend.dto.alumni;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Institution-controlled fields only — the admin counterpart of
 * AlumniSelfUpdateRequest. Used for manual single-record creation; CSV rows
 * are mapped into the same fields by AlumniImportService.
 */
@Getter
@Setter
public class AdminAlumniCreateRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Name can be at most 150 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    @Size(max = 255, message = "Email can be at most 255 characters")
    private String email;

    @NotBlank(message = "Enrollment number is required")
    @Size(max = 30, message = "Enrollment number can be at most 30 characters")
    private String enrollmentNumber;

    @Size(max = 100, message = "Degree can be at most 100 characters")
    private String degree;

    @NotNull(message = "Department is required")
    private Long departmentId;

    @Size(max = 20, message = "Batch can be at most 20 characters")
    private String batch;

    @NotNull(message = "Graduation year is required")
    private Integer graduationYear;
}
