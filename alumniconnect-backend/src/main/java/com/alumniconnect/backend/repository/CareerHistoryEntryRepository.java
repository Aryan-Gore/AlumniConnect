package com.alumniconnect.backend.repository;

import com.alumniconnect.backend.entity.CareerHistoryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CareerHistoryEntryRepository extends JpaRepository<CareerHistoryEntry, Long> {
    List<CareerHistoryEntry> findByAlumniRecordIdOrderByStartDateDesc(Long alumniId);

    Optional<CareerHistoryEntry> findByIdAndAlumniRecordId(Long id, Long alumniId);
}
