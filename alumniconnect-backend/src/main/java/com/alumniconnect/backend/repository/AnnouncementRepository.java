package com.alumniconnect.backend.repository;

import com.alumniconnect.backend.entity.Announcement;
import com.alumniconnect.backend.entity.AnnouncementStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByStatusOrderByPublishDateDesc(AnnouncementStatus status);

    List<Announcement> findAllByOrderByPublishDateDescIdDesc();
}
