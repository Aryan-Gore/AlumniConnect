package com.alumniconnect.backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleRegisterRequest {

    @NotBlank(message = "Registration token is required")
    private String registrationToken;

    @NotBlank(message = "Enrollment number is required")
    @Size(max = 30, message = "Enrollment number can be at most 30 characters")
    private String enrollmentNumber;

    @NotNull(message = "Department is required")
    private Long departmentId;

    @Size(max = 20, message = "Batch can be at most 20 characters")
    private String batch;
}
