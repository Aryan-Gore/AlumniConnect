package com.alumniconnect.backend.service;

import com.alumniconnect.backend.dto.common.PublicStatsResponse;
import com.alumniconnect.backend.entity.EventStatus;
import com.alumniconnect.backend.entity.JobStatus;
import com.alumniconnect.backend.entity.Role;
import com.alumniconnect.backend.repository.AlumniRecordRepository;
import com.alumniconnect.backend.repository.DepartmentRepository;
import com.alumniconnect.backend.repository.EventRepository;
import com.alumniconnect.backend.repository.InstituteRepository;
import com.alumniconnect.backend.repository.JobPostingRepository;
import com.alumniconnect.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Public, unauthenticated aggregates for the landing page. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicStatsService {

    private final AlumniRecordRepository alumniRecordRepository;
    private final UserRepository userRepository;
    private final InstituteRepository instituteRepository;
    private final DepartmentRepository departmentRepository;
    private final EventRepository eventRepository;
    private final JobPostingRepository jobPostingRepository;

    public PublicStatsResponse stats() {
        return PublicStatsResponse.builder()
                .alumniRecords(alumniRecordRepository.count())
                .alumniMembers(userRepository.countByRoleAndActiveTrue(Role.ALUMNI))
                .studentMembers(userRepository.countByRoleAndActiveTrue(Role.STUDENT))
                .institutes(instituteRepository.count())
                .departments(departmentRepository.count())
                .upcomingEvents(eventRepository.countByStatusAndEventDateAfter(EventStatus.PUBLISHED, LocalDateTime.now()))
                .openJobs(jobPostingRepository.countOpen(JobStatus.APPROVED, LocalDate.now()))
                .build();
    }
}
