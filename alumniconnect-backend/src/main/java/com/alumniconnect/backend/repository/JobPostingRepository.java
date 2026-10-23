package com.alumniconnect.backend.repository;

import com.alumniconnect.backend.entity.JobPosting;
import com.alumniconnect.backend.entity.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    List<JobPosting> findByStatusOrderByPostedAtDesc(JobStatus status);

    /** Listings in the given status that haven't passed their expiry date (no expiry date = open-ended). */
    @Query("select j from JobPosting j where j.status = :status and (j.expiresAt is null or j.expiresAt >= :today) "
            + "order by j.postedAt desc")
    List<JobPosting> findOpen(@Param("status") JobStatus status, @Param("today") LocalDate today);

    @Query("select count(j) from JobPosting j where j.status = :status and (j.expiresAt is null or j.expiresAt >= :today)")
    long countOpen(@Param("status") JobStatus status, @Param("today") LocalDate today);

    /** Everything one user has posted, whatever its moderation status. */
    List<JobPosting> findByPostedByIdOrderByPostedAtDesc(Long userId);

    long countByStatus(JobStatus status);
}
