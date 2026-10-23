package com.alumniconnect.backend.service;

import com.alumniconnect.backend.dto.common.InstituteResponse;
import com.alumniconnect.backend.entity.Department;
import com.alumniconnect.backend.entity.Institute;
import com.alumniconnect.backend.exception.BadRequestException;
import com.alumniconnect.backend.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Institute → Department lookups. SVVV is organised as institutes that each
 * contain departments; every other part of the system refers to a
 * department by id and gets its institute through it.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public List<InstituteResponse> listInstitutesWithDepartments() {
        Map<Long, Institute> institutes = new LinkedHashMap<>();
        Map<Long, List<InstituteResponse.DepartmentItem>> byInstitute = new LinkedHashMap<>();

        for (Department d : departmentRepository.findAllWithInstitute()) {
            Institute institute = d.getInstitute();
            institutes.putIfAbsent(institute.getId(), institute);
            byInstitute.computeIfAbsent(institute.getId(), id -> new ArrayList<>())
                    .add(InstituteResponse.DepartmentItem.builder()
                            .id(d.getId()).name(d.getName()).code(d.getCode()).build());
        }

        List<InstituteResponse> result = new ArrayList<>();
        for (Map.Entry<Long, Institute> entry : institutes.entrySet()) {
            Institute institute = entry.getValue();
            result.add(InstituteResponse.builder()
                    .id(institute.getId())
                    .name(institute.getName())
                    .code(institute.getCode())
                    .departments(byInstitute.get(entry.getKey()))
                    .build());
        }
        return result;
    }

    /** For request payloads: a missing/unknown department id is the caller's mistake, hence 400 rather than 404. */
    public Department require(Long departmentId) {
        if (departmentId == null) {
            throw new BadRequestException("Choose a department.");
        }
        return departmentRepository.findByIdWithInstitute(departmentId)
                .orElseThrow(() -> new BadRequestException("That department doesn't exist."));
    }

    /** One query up front; the resolver then answers every CSV row from memory. */
    public DepartmentResolver importResolver() {
        return new DepartmentResolver(departmentRepository.findAllWithInstitute());
    }
}
