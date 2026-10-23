package com.alumniconnect.backend.dto.alumni;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ImportUploadResponse {
    private Long importId;
    private List<String> detectedColumns;
    private int totalRows;
}
