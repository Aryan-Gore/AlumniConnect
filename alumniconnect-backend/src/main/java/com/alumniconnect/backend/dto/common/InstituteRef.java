package com.alumniconnect.backend.dto.common;

import com.alumniconnect.backend.entity.Institute;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class InstituteRef {
    private Long id;
    private String name;
    private String code;

    public static InstituteRef from(Institute institute) {
        if (institute == null) return null;
        return InstituteRef.builder()
                .id(institute.getId())
                .name(institute.getName())
                .code(institute.getCode())
                .build();
    }
}
