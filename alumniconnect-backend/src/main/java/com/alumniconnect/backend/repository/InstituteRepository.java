package com.alumniconnect.backend.repository;

import com.alumniconnect.backend.entity.Institute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstituteRepository extends JpaRepository<Institute, Long> {
    List<Institute> findAllByOrderByNameAsc();
}
