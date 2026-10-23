package com.alumniconnect.backend.repository;

import com.alumniconnect.backend.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByIdAndAlumniRecordId(Long id, Long alumniId);
}
