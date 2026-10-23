package com.alumniconnect.backend.dto.alumni;

import com.alumniconnect.backend.entity.AlumniImportBatch;
import com.alumniconnect.backend.entity.ImportBatchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class ImportBatchSummaryResponse {
    private Long id;
    private String originalFilename;
    private ImportBatchStatus status;
    private Integer totalRows;
    private Integer successfulRows;
    private Integer failedRows;
    private Integer duplicateRows;
    private String adminEmail;
    private Instant createdAt;

    public static ImportBatchSummaryResponse from(AlumniImportBatch b) {
        return ImportBatchSummaryResponse.builder()
                .id(b.getId())
                .originalFilename(b.getOriginalFilename())
                .status(b.getStatus())
                .totalRows(b.getTotalRows())
                .successfulRows(b.getSuccessfulRows())
                .failedRows(b.getFailedRows())
                .duplicateRows(b.getDuplicateRows())
                .adminEmail(b.getAdmin().getEmail())
                .createdAt(b.getCreatedAt())
                .build();
    }
}
