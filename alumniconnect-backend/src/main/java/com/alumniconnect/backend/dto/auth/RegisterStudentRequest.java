package com.alumniconnect.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterStudentRequest {

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

    @NotNull(message = "Department is required")
    private Long departmentId;

    @Size(max = 20, message = "Batch can be at most 20 characters")
    private String batch;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be 8–100 characters")
    private String password;
}
