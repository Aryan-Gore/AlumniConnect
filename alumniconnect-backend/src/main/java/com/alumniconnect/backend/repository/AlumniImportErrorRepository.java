package com.alumniconnect.backend.repository;

import com.alumniconnect.backend.entity.AlumniImportError;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlumniImportErrorRepository extends JpaRepository<AlumniImportError, Long> {
    List<AlumniImportError> findByBatchId(Long batchId);
}
