package com.alumniconnect.backend.dto.alumni;

import com.alumniconnect.backend.entity.CareerHistoryEntry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class CareerHistoryResponse {
    private Long id;
    private String company;
    private String jobTitle;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;

    public static CareerHistoryResponse from(CareerHistoryEntry entry) {
        return CareerHistoryResponse.builder()
                .id(entry.getId())
                .company(entry.getCompany())
                .jobTitle(entry.getJobTitle())
                .startDate(entry.getStartDate())
                .endDate(entry.getEndDate())
                .description(entry.getDescription())
                .build();
    }
}
