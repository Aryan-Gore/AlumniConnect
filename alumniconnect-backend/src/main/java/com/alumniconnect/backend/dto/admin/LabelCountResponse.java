package com.alumniconnect.backend.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LabelCountResponse {
    private String label;
    private long value;
}
