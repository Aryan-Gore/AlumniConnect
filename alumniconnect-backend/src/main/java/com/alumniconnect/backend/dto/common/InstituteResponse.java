package com.alumniconnect.backend.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/** One institute with its departments — the data behind every Institute → Department picker. */
@Getter
@Builder
@AllArgsConstructor
public class InstituteResponse {
    private Long id;
    private String name;
    private String code;
    private List<DepartmentItem> departments;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class DepartmentItem {
        private Long id;
        private String name;
        private String code;
    }
}
