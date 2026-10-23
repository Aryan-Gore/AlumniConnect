package com.alumniconnect.backend.controller;

import com.alumniconnect.backend.dto.common.InstituteResponse;
import com.alumniconnect.backend.service.DepartmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/institutes")
@RequiredArgsConstructor
@Tag(name = "Institutes & Departments", description = "The SVVV Institute → Department structure (public, needed by the sign-up form)")
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<List<InstituteResponse>> list() {
        return ResponseEntity.ok(departmentService.listInstitutesWithDepartments());
    }
}
