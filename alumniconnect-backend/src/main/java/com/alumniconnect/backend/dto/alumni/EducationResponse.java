package com.alumniconnect.backend.dto.alumni;

import com.alumniconnect.backend.entity.EducationRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class EducationResponse {
    private Long id;
    private String degree;
    private String program;
    private String institutionName;
    private Integer startYear;
    private Integer endYear;
    private BigDecimal cgpa;

    public static EducationResponse from(EducationRecord e) {
        return EducationResponse.builder()
                .id(e.getId())
                .degree(e.getDegree())
                .program(e.getProgram())
                .institutionName(e.getInstitutionName())
                .startYear(e.getStartYear())
                .endYear(e.getEndYear())
                .cgpa(e.getCgpa())
                .build();
    }
}
