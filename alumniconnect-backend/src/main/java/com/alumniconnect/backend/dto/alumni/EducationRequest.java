package com.alumniconnect.backend.dto.alumni;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class EducationRequest {

    @NotBlank(message = "Degree is required")
    @Size(max = 100, message = "Degree can be at most 100 characters")
    private String degree;

    @Size(max = 150, message = "Program can be at most 150 characters")
    private String program;

    @NotBlank(message = "Institution is required")
    @Size(max = 200, message = "Institution can be at most 200 characters")
    private String institutionName;

    @Min(value = 1950, message = "Start year looks too early")
    @Max(value = 2100, message = "Start year looks too far ahead")
    private Integer startYear;

    @Min(value = 1950, message = "End year looks too early")
    @Max(value = 2100, message = "End year looks too far ahead")
    private Integer endYear;

    @DecimalMin(value = "0.0", message = "CGPA can't be negative")
    @DecimalMax(value = "100.0", message = "CGPA can be at most 100")
    private BigDecimal cgpa;
}
