package com.alumniconnect.backend.repository;

import com.alumniconnect.backend.entity.EducationRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EducationRecordRepository extends JpaRepository<EducationRecord, Long> {
    Optional<EducationRecord> findByIdAndAlumniRecordId(Long id, Long alumniId);
}
