package com.alumniconnect.backend.dto.alumni;

import com.alumniconnect.backend.entity.Certification;
import com.alumniconnect.backend.entity.CertificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class CertificationResponse {
    private Long id;
    private CertificationType type;
    private String title;
    private String issuer;
    private LocalDate issueDate;
    private String credentialUrl;

    public static CertificationResponse from(Certification c) {
        return CertificationResponse.builder()
                .id(c.getId())
                .type(c.getType())
                .title(c.getTitle())
                .issuer(c.getIssuer())
                .issueDate(c.getIssueDate())
                .credentialUrl(c.getCredentialUrl())
                .build();
    }
}
