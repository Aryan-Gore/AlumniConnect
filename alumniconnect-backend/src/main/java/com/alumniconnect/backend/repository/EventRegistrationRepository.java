package com.alumniconnect.backend.repository;

import com.alumniconnect.backend.entity.EventRegistration;
import com.alumniconnect.backend.entity.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    Optional<EventRegistration> findByEventIdAndUserId(Long eventId, Long userId);
    List<EventRegistration> findByUserIdAndStatus(Long userId, RegistrationStatus status);
    List<EventRegistration> findByEventIdAndStatus(Long eventId, RegistrationStatus status);
    long countByEventIdAndStatus(Long eventId, RegistrationStatus status);
}
