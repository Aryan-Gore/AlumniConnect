package com.alumniconnect.backend.dto.alumni;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ColumnMappingRequest {
    /** CSV column header -> canonical system field key (see AlumniImportService.SYSTEM_FIELDS). */
    @NotEmpty(message = "mapping is required")
    private Map<String, String> mapping;
}
