package com.alumniconnect.backend.repository;

import com.alumniconnect.backend.entity.AlumniImportBatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlumniImportBatchRepository extends JpaRepository<AlumniImportBatch, Long> {
    List<AlumniImportBatch> findAllByOrderByCreatedAtDesc();
}
