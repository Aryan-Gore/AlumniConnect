package com.alumniconnect.backend.service;

import com.alumniconnect.backend.entity.Department;
import com.alumniconnect.backend.exception.BadRequestException;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves the free-text department (and optional institute) cells of a CSV
 * row to a real Department. Built once per import from a single query, so a
 * 5,000-row file costs one lookup, not 5,000.
 */
public class DepartmentResolver {

    private final List<Department> all;

    DepartmentResolver(List<Department> all) {
        this.all = all;
    }

    /**
     * The department column may hold the full name ("Computer Science &amp;
     * Engineering") or the short code ("CSE"), in any case. If an institute
     * column was mapped too it narrows the match (institute name or code).
     *
     * @return the single matching department, or null when nothing matches
     * @throws BadRequestException when the text matches more than one department
     */
    public Department resolve(String departmentText, String instituteText) {
        if (departmentText == null || departmentText.isBlank()) return null;
        String wanted = departmentText.trim();
        String wantedInstitute = instituteText == null || instituteText.isBlank() ? null : instituteText.trim();

        List<Department> matches = new ArrayList<>();
        for (Department d : all) {
            boolean nameOrCode = d.getName().equalsIgnoreCase(wanted)
                    || (d.getCode() != null && d.getCode().equalsIgnoreCase(wanted));
            if (!nameOrCode) continue;
            if (wantedInstitute != null
                    && !d.getInstitute().getName().equalsIgnoreCase(wantedInstitute)
                    && !d.getInstitute().getCode().equalsIgnoreCase(wantedInstitute)) {
                continue;
            }
            matches.add(d);
        }
        if (matches.size() > 1) {
            throw new BadRequestException("Department \"" + wanted + "\" exists in more than one institute — "
                    + "map an institute column or use the full department name.");
        }
        return matches.isEmpty() ? null : matches.get(0);
    }
}
