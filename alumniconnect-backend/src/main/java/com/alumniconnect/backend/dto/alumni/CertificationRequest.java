package com.alumniconnect.backend.dto.alumni;

import com.alumniconnect.backend.entity.CertificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CertificationRequest {

    @NotNull(message = "Type is required")
    private CertificationType type;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title can be at most 200 characters")
    private String title;

    @Size(max = 150, message = "Issuer can be at most 150 characters")
    private String issuer;

    private LocalDate issueDate;

    @Size(max = 255, message = "URL can be at most 255 characters")
    @Pattern(regexp = "^(|https?://\\S+)$", message = "Enter a full http(s) URL")
    private String credentialUrl;
}
