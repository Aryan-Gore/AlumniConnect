package com.alumniconnect.backend.repository;

import com.alumniconnect.backend.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /** Every department with its institute already loaded, ordered the way the pickers show them. */
    @Query("select d from Department d join fetch d.institute i order by i.name asc, d.name asc")
    List<Department> findAllWithInstitute();

    @Query("select d from Department d join fetch d.institute where d.id = :id")
    Optional<Department> findByIdWithInstitute(@Param("id") Long id);
}
