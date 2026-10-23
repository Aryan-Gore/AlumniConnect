package com.alumniconnect.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClaimConfirmRequest {

    @NotNull(message = "alumniId is required")
    private Long alumniId;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Enrollment number is required")
    private String enrollmentNumber;

    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Graduation year is required")
    private String graduationYear;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
