package com.alumniconnect.backend.service;

import com.alumniconnect.backend.dto.common.DepartmentRef;
import com.alumniconnect.backend.dto.mentorship.MentorshipResponse;
import com.alumniconnect.backend.entity.*;
import com.alumniconnect.backend.exception.BadRequestException;
import com.alumniconnect.backend.exception.DuplicateResourceException;
import com.alumniconnect.backend.exception.ResourceNotFoundException;
import com.alumniconnect.backend.repository.AlumniRecordRepository;
import com.alumniconnect.backend.repository.MentorshipRequestRepository;
import com.alumniconnect.backend.repository.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MentorshipService {

    private final MentorshipRequestRepository mentorshipRequestRepository;
    private final AlumniRecordRepository alumniRecordRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final NotificationService notificationService;

    @Transactional
    public MentorshipResponse sendRequest(User student, Long alumniId, String message) {
        AlumniRecord alumni = alumniRecordRepository.findById(alumniId)
                .orElseThrow(() -> new ResourceNotFoundException("Alumni record not found."));

        if (alumni.getUser() == null || (alumni.getStatus() != AlumniStatus.CLAIMED && alumni.getStatus() != AlumniStatus.ACTIVE)) {
            throw new BadRequestException("This alumnus hasn't joined AlumniConnect yet.");
        }
        if (!alumni.isOpenToMentoring()) {
            throw new BadRequestException("This alumnus isn't currently open to mentorship requests.");
        }

        boolean alreadyPending = mentorshipRequestRepository.findByStudentIdOrderByRequestedAtDesc(student.getId())
                .stream().anyMatch(r -> r.getAlumni().getId().equals(alumniId) && r.getStatus() == MentorshipStatus.PENDING);
        if (alreadyPending) {
            throw new DuplicateResourceException("You already have a pending request with this alumnus.");
        }

        MentorshipRequest request = mentorshipRequestRepository.save(MentorshipRequest.builder()
                .student(student)
                .alumni(alumni)
                .message(message)
                .status(MentorshipStatus.PENDING)
                .build());

        if (alumni.getUser() != null) {
            notificationService.notify(alumni.getUser(), NotificationCategory.MENTORSHIP,
                    student.getFullName() + " requested mentorship from you.");
        }

        return toResponse(request);
    }

    public List<MentorshipResponse> myRequests(Long studentId) {
        return mentorshipRequestRepository.findByStudentIdOrderByRequestedAtDesc(studentId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<MentorshipResponse> receivedRequests(Long alumniUserId) {
        AlumniRecord alumni = alumniRecordRepository.findByUserId(alumniUserId)
                .orElseThrow(() -> new ResourceNotFoundException("No alumni record is linked to this account."));
        return mentorshipRequestRepository.findByAlumniIdOrderByRequestedAtDesc(alumni.getId()).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public MentorshipResponse respondToRequest(Long requestId, Long alumniUserId, String decision) {
        MentorshipRequest request = findOrThrow(requestId);
        assertOwnedByAlumniUser(request, alumniUserId);

        MentorshipStatus newStatus;
        try {
            newStatus = MentorshipStatus.valueOf(decision.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("decision must be ACCEPTED or REJECTED.");
        }
        if (newStatus != MentorshipStatus.ACCEPTED && newStatus != MentorshipStatus.REJECTED) {
            throw new BadRequestException("decision must be ACCEPTED or REJECTED.");
        }
        if (request.getStatus() != MentorshipStatus.PENDING) {
            throw new BadRequestException("This request has already been responded to.");
        }

        request.setStatus(newStatus);
        request.setRespondedAt(Instant.now());
        mentorshipRequestRepository.save(request);

        notificationService.notify(request.getStudent(), NotificationCategory.MENTORSHIP,
                request.getAlumni().getFullName() + (newStatus == MentorshipStatus.ACCEPTED
                        ? " accepted your mentorship request." : " declined your mentorship request."));

        return toResponse(request);
    }

    @Transactional
    public MentorshipResponse markCompleted(Long requestId, Long alumniUserId) {
        MentorshipRequest request = findOrThrow(requestId);
        assertOwnedByAlumniUser(request, alumniUserId);

        if (request.getStatus() != MentorshipStatus.ACCEPTED) {
            throw new BadRequestException("Only an accepted mentorship can be marked complete.");
        }
        request.setStatus(MentorshipStatus.COMPLETED);
        mentorshipRequestRepository.save(request);
        return toResponse(request);
    }

    private void assertOwnedByAlumniUser(MentorshipRequest request, Long alumniUserId) {
        if (request.getAlumni().getUser() == null || !request.getAlumni().getUser().getId().equals(alumniUserId)) {
            throw new ResourceNotFoundException("Mentorship request not found.");
        }
    }

    private MentorshipResponse toResponse(MentorshipRequest request) {
        var studentProfile = studentProfileRepository.findByUserId(request.getStudent().getId()).orElse(null);
        var studentSummary = MentorshipResponse.StudentSummary.builder()
                .id(request.getStudent().getId())
                .fullName(request.getStudent().getFullName())
                .department(studentProfile != null ? DepartmentRef.from(studentProfile.getDepartment()) : null)
                .batch(studentProfile != null ? studentProfile.getBatch() : null)
                .profilePhotoUrl(request.getStudent().getProfilePhotoUrl())
                .build();
        return MentorshipResponse.from(request, studentSummary);
    }

    private MentorshipRequest findOrThrow(Long id) {
        return mentorshipRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mentorship request " + id + " not found."));
    }
}
