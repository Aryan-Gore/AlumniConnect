package com.alumniconnect.backend.repository;

import com.alumniconnect.backend.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    Optional<StudentProfile> findByUserId(Long userId);

    boolean existsByEnrollmentNumber(String enrollmentNumber);

    @Query("select s from StudentProfile s join fetch s.department d join fetch d.institute where s.user.id = :userId")
    Optional<StudentProfile> findWithDepartmentByUserId(@Param("userId") Long userId);

    @Query("select s from StudentProfile s join fetch s.department d join fetch d.institute where s.user.id in :userIds")
    List<StudentProfile> findWithDepartmentByUserIdIn(@Param("userIds") Collection<Long> userIds);

    List<StudentProfile> findByDepartmentId(Long departmentId);

    /** Rows of [userId, departmentId] — no entity loading, for callers that only need to know who belongs where. */
    @Query("select s.user.id, s.department.id from StudentProfile s where s.user.id in :userIds")
    List<Object[]> findDepartmentIdsByUserIdIn(@Param("userIds") Collection<Long> userIds);
}
