package com.alumniconnect.backend.repository;

import com.alumniconnect.backend.entity.AlumniRecord;
import com.alumniconnect.backend.entity.AlumniStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AlumniRecordRepository extends JpaRepository<AlumniRecord, Long>, JpaSpecificationExecutor<AlumniRecord> {
    Optional<AlumniRecord> findByUserId(Long userId);

    Optional<AlumniRecord> findByEnrollmentNumber(String enrollmentNumber);

    boolean existsByEnrollmentNumber(String enrollmentNumber);

    boolean existsByEmailIgnoreCase(String email);

    // Used by the claim-lookup flow: match on enrollment number + email +
    // graduation year, and only against a record nobody has claimed yet.
    Optional<AlumniRecord> findByEnrollmentNumberAndEmailIgnoreCaseAndGraduationYearAndUserIsNull(
            String enrollmentNumber, String email, Integer graduationYear);

    long countByStatus(AlumniStatus status);

    /** The signed-in alumnus' own record with department and institute already loaded. */
    @Query("select a from AlumniRecord a join fetch a.department d join fetch d.institute where a.user.id = :userId")
    Optional<AlumniRecord> findWithDepartmentByUserId(@Param("userId") Long userId);

    /** Batch lookup used to attach headline/department to a list of users in one query. */
    @Query("select a from AlumniRecord a join fetch a.department d join fetch d.institute where a.user.id in :userIds")
    List<AlumniRecord> findWithDepartmentByUserIdIn(@Param("userIds") Collection<Long> userIds);

    /** Rows of [userId, departmentId] for claimed records — no entity loading. */
    @Query("select a.user.id, a.department.id from AlumniRecord a where a.user.id in :userIds")
    List<Object[]> findDepartmentIdsByUserIdIn(@Param("userIds") Collection<Long> userIds);

    // ---- Dashboard aggregates: each row is [label, count] ----

    @Query("select concat(d.name, ' (', i.code, ')'), count(a) from AlumniRecord a "
            + "join a.department d join d.institute i group by d.id, d.name, i.code order by count(a) desc")
    List<Object[]> countGroupedByDepartment();

    @Query("select i.name, count(a) from AlumniRecord a join a.department d join d.institute i "
            + "group by i.id, i.name order by count(a) desc")
    List<Object[]> countGroupedByInstitute();

    @Query("select a.graduationYear, count(a) from AlumniRecord a group by a.graduationYear order by a.graduationYear asc")
    List<Object[]> countGroupedByGraduationYear();

    @Query("select a.industry, count(a) from AlumniRecord a where a.industry is not null "
            + "group by a.industry order by count(a) desc")
    List<Object[]> countGroupedByIndustry();

    @Query("select a.currentLocation, count(a) from AlumniRecord a where a.currentLocation is not null "
            + "group by a.currentLocation order by count(a) desc")
    List<Object[]> countGroupedByLocation();
}
