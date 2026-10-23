package com.alumniconnect.backend.repository;

import com.alumniconnect.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

/**
 * Group-member queries that span StudentProfile and AlumniRecord.
 * Uses a separate repository to avoid bloating existing repositories.
 */
public interface GroupQueryRepository extends JpaRepository<User, Long> {

    /**
     * Count active students in a department.
     */
    @Query("""
            select count(sp) from StudentProfile sp
            where sp.department.id = :deptId and sp.user.active = true
            """)
    long countStudents(@Param("deptId") Long deptId);

    /**
     * Count active claimed alumni in a department.
     */
    @Query("""
            select count(ar) from AlumniRecord ar
            where ar.department.id = :deptId
              and ar.status = com.alumniconnect.backend.entity.AlumniStatus.ACTIVE
              and ar.user is not null
              and ar.user.active = true
            """)
    long countAlumni(@Param("deptId") Long deptId);

    /**
     * Batch: student counts for multiple departments — returns [deptId, count].
     */
    @Query("""
            select sp.department.id, count(sp) from StudentProfile sp
            where sp.department.id in :deptIds and sp.user.active = true
            group by sp.department.id
            """)
    List<Object[]> studentCountsByDepartments(@Param("deptIds") Collection<Long> deptIds);

    /**
     * Batch: alumni counts for multiple departments — returns [deptId, count].
     */
    @Query("""
            select ar.department.id, count(ar) from AlumniRecord ar
            where ar.department.id in :deptIds
              and ar.status = com.alumniconnect.backend.entity.AlumniStatus.ACTIVE
              and ar.user is not null
              and ar.user.active = true
            group by ar.department.id
            """)
    List<Object[]> alumniCountsByDepartments(@Param("deptIds") Collection<Long> deptIds);

    /**
     * Members of a department: students + claimed active alumni, sorted by fullName.
     * Alumni hidden by directory visibility are filtered by the visible set.
     */
    @Query("""
            select u from User u
            where u.active = true
              and (
                exists (select 1 from StudentProfile sp where sp.user = u and sp.department.id = :deptId)
                or exists (
                  select 1 from AlumniRecord ar
                  where ar.user = u
                    and ar.department.id = :deptId
                    and ar.status = com.alumniconnect.backend.entity.AlumniStatus.ACTIVE
                    and ar.directoryVisibility in :visible
                )
              )
              and (:query = '' or lower(u.fullName) like lower(concat('%', :query, '%')))
            order by u.fullName asc
            """)
    Page<User> findMembers(
            @Param("deptId") Long deptId,
            @Param("visible") Collection<com.alumniconnect.backend.entity.DirectoryVisibility> visible,
            @Param("query") String query,
            Pageable pageable);
}
