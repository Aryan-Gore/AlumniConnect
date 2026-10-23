package com.alumniconnect.backend.dto.alumni;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class ImportPreviewRowResponse {
    private int rowIndex;
    private Map<String, String> values;
    /** READY, DUPLICATE, or ERROR */
    private String flag;
    private String errorMessage;
}
