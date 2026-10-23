package com.alumniconnect.backend.repository;

import com.alumniconnect.backend.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CertificationRepository extends JpaRepository<Certification, Long> {
    Optional<Certification> findByIdAndAlumniRecordId(Long id, Long alumniId);
}
