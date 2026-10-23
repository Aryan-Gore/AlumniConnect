package com.alumniconnect.backend.dto.alumni;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ImportPreviewResponse {
    private int totalRows;
    private long readyCount;
    private long duplicateCount;
    private long errorCount;
    private List<ImportPreviewRowResponse> sampleRows;
}
