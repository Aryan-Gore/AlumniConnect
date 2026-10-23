package com.alumniconnect.backend.repository;

import com.alumniconnect.backend.entity.MentorshipRequest;
import com.alumniconnect.backend.entity.MentorshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MentorshipRequestRepository extends JpaRepository<MentorshipRequest, Long> {
    List<MentorshipRequest> findByStudentIdOrderByRequestedAtDesc(Long studentId);
    List<MentorshipRequest> findByAlumniIdOrderByRequestedAtDesc(Long alumniId);
    List<MentorshipRequest> findByAlumniIdAndStatusOrderByRequestedAtDesc(Long alumniId, MentorshipStatus status);
    long countByAlumniIdAndStatus(Long alumniId, MentorshipStatus status);
    long countByStatus(MentorshipStatus status);
}
