package com.alumniconnect.backend.service;

import com.alumniconnect.backend.entity.AlumniRecord;
import com.alumniconnect.backend.entity.AlumniStatus;
import com.alumniconnect.backend.entity.DirectoryVisibility;
import com.alumniconnect.backend.entity.Role;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class AlumniSpecifications {

    private AlumniSpecifications() {
    }

    /** Matches name, company, job title or any skill — the single search box of the directory. */
    public static Specification<AlumniRecord> keywordMatches(String query) {
        if (query == null || query.isBlank()) return null;
        String like = "%" + query.trim().toLowerCase() + "%";
        return (root, cq, cb) -> {
            cq.distinct(true);
            Join<Object, Object> skills = root.join("skills", JoinType.LEFT);
            return cb.or(
                    cb.like(cb.lower(root.get("fullName")), like),
                    cb.like(cb.lower(root.get("currentCompany")), like),
                    cb.like(cb.lower(root.get("jobTitle")), like),
                    cb.like(cb.lower(skills.get("name")), like)
            );
        };
    }

    public static Specification<AlumniRecord> hasDepartmentId(Long departmentId) {
        if (departmentId == null) return null;
        return (root, cq, cb) -> cb.equal(root.get("department").get("id"), departmentId);
    }

    public static Specification<AlumniRecord> hasInstituteId(Long instituteId) {
        if (instituteId == null) return null;
        return (root, cq, cb) -> cb.equal(root.get("department").get("institute").get("id"), instituteId);
    }

    public static Specification<AlumniRecord> hasBatch(String batch) {
        if (batch == null || batch.isBlank()) return null;
        return (root, cq, cb) -> cb.equal(root.get("batch"), batch.trim());
    }

    public static Specification<AlumniRecord> hasGraduationYear(Integer year) {
        if (year == null) return null;
        return (root, cq, cb) -> cb.equal(root.get("graduationYear"), year);
    }

    public static Specification<AlumniRecord> hasCompany(String company) {
        if (company == null || company.isBlank()) return null;
        return (root, cq, cb) -> cb.like(cb.lower(root.get("currentCompany")), "%" + company.trim().toLowerCase() + "%");
    }

    public static Specification<AlumniRecord> hasIndustry(String industry) {
        if (industry == null || industry.isBlank()) return null;
        return (root, cq, cb) -> cb.equal(root.get("industry"), industry.trim());
    }

    public static Specification<AlumniRecord> hasLocation(String location) {
        if (location == null || location.isBlank()) return null;
        return (root, cq, cb) -> cb.like(cb.lower(root.get("currentLocation")), "%" + location.trim().toLowerCase() + "%");
    }

    /** One call per skill: passing two skills means the alumnus must know both. */
    public static Specification<AlumniRecord> hasSkill(String skill) {
        if (skill == null || skill.isBlank()) return null;
        String wanted = skill.trim().toLowerCase();
        return (root, cq, cb) -> {
            cq.distinct(true);
            Join<Object, Object> skills = root.join("skills", JoinType.INNER);
            return cb.equal(cb.lower(skills.get("name")), wanted);
        };
    }

    public static Specification<AlumniRecord> isOpenToMentoring(Boolean flag) {
        if (flag == null || !flag) return null;
        return (root, cq, cb) -> cb.isTrue(root.get("openToMentoring"));
    }

    public static Specification<AlumniRecord> isAvailableForHiring(Boolean flag) {
        if (flag == null || !flag) return null;
        return (root, cq, cb) -> cb.isTrue(root.get("availableForHiring"));
    }

    public static Specification<AlumniRecord> hasStatusIn(List<AlumniStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) return null;
        return (root, cq, cb) -> root.get("status").in(statuses);
    }

    /**
     * The directory-privacy rule from the SRS, made concrete: PRIVATE is
     * never listed (the owner reaches their own profile through /alumni/me);
     * ALUMNI_ONLY hides from students; everything else is visible to any
     * authenticated role. Call this only for the non-admin search path —
     * admins bypass it entirely.
     */
    public static Specification<AlumniRecord> visibleTo(Role viewerRole) {
        List<DirectoryVisibility> visible = visibleStatesFor(viewerRole);
        return (root, cq, cb) -> root.get("directoryVisibility").in(visible);
    }

    /**
     * Which directory-visibility levels a viewer with this role may see. Shared by
     * everything that lists people (the directory, chat user search, group members)
     * so "who can discover whom" is decided in exactly one place. Admins see every level.
     */
    public static List<DirectoryVisibility> visibleStatesFor(Role viewerRole) {
        if (viewerRole == Role.ADMIN) {
            return List.of(DirectoryVisibility.values());
        }
        if (viewerRole == Role.STUDENT) {
            return List.of(DirectoryVisibility.PUBLIC, DirectoryVisibility.STUDENTS_AND_ALUMNI);
        }
        return List.of(DirectoryVisibility.PUBLIC, DirectoryVisibility.STUDENTS_AND_ALUMNI, DirectoryVisibility.ALUMNI_ONLY);
    }
}
