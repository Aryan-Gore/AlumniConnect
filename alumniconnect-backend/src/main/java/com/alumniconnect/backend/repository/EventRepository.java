package com.alumniconnect.backend.repository;

import com.alumniconnect.backend.entity.Event;
import com.alumniconnect.backend.entity.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByStatusOrderByEventDateAsc(EventStatus status);
    long countByStatus(EventStatus status);

    long countByStatusAndEventDateAfter(EventStatus status, LocalDateTime after);
}
