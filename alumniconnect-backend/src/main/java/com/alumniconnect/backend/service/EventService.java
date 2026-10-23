package com.alumniconnect.backend.service;

import com.alumniconnect.backend.dto.event.EventRequest;
import com.alumniconnect.backend.dto.event.EventResponse;
import com.alumniconnect.backend.entity.*;
import com.alumniconnect.backend.exception.BadRequestException;
import com.alumniconnect.backend.exception.DuplicateResourceException;
import com.alumniconnect.backend.exception.ResourceNotFoundException;
import com.alumniconnect.backend.repository.EventRegistrationRepository;
import com.alumniconnect.backend.repository.EventRepository;
import com.alumniconnect.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    public List<EventResponse> listPublished(Long viewerUserId) {
        return eventRepository.findByStatusOrderByEventDateAsc(EventStatus.PUBLISHED).stream()
                .map(e -> toResponse(e, viewerUserId))
                .collect(Collectors.toList());
    }

    public EventResponse getById(Long id, Long viewerUserId) {
        return toResponse(findOrThrow(id), viewerUserId);
    }

    public List<EventResponse> myRegistrations(Long userId) {
        return registrationRepository.findByUserIdAndStatus(userId, RegistrationStatus.REGISTERED).stream()
                .map(reg -> toResponse(reg.getEvent(), userId))
                .collect(Collectors.toList());
    }

    @Transactional
    public EventResponse register(Long eventId, User user) {
        Event event = findOrThrow(eventId);
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BadRequestException("This event isn't open for registration.");
        }
        if (registrationRepository.findByEventIdAndUserId(eventId, user.getId())
                .filter(r -> r.getStatus() == RegistrationStatus.REGISTERED).isPresent()) {
            throw new DuplicateResourceException("You're already registered for this event.");
        }

        long currentCount = registrationRepository.countByEventIdAndStatus(eventId, RegistrationStatus.REGISTERED);
        if (currentCount >= event.getCapacity()) {
            throw new BadRequestException("This event is at capacity.");
        }

        registrationRepository.findByEventIdAndUserId(eventId, user.getId())
                .ifPresentOrElse(
                        existing -> {
                            existing.setStatus(RegistrationStatus.REGISTERED);
                            registrationRepository.save(existing);
                        },
                        () -> registrationRepository.save(EventRegistration.builder()
                                .event(event).user(user).status(RegistrationStatus.REGISTERED).build())
                );

        return toResponse(event, user.getId());
    }

    @Transactional
    public EventResponse cancelRegistration(Long eventId, Long userId) {
        EventRegistration registration = registrationRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("You're not registered for this event."));
        registration.setStatus(RegistrationStatus.CANCELLED);
        registrationRepository.save(registration);
        return toResponse(registration.getEvent(), userId);
    }

    // ------------------------------------------------------------------
    // Admin
    // ------------------------------------------------------------------

    public List<EventResponse> adminList() {
        return eventRepository.findAll().stream().map(e -> toResponse(e, null)).collect(Collectors.toList());
    }

    @Transactional
    public EventResponse adminCreate(EventRequest request, User admin) {
        Event event = eventRepository.save(Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .eventDate(request.getEventDate())
                .mode(request.getMode())
                .location(request.getLocation())
                .capacity(request.getCapacity())
                .status(EventStatus.DRAFT)
                .createdBy(admin)
                .build());

        auditLogService.record(admin, AuditAction.CREATE_EVENT, event.getTitle());
        return toResponse(event, null);
    }

    @Transactional
    public EventResponse adminUpdate(Long id, EventRequest request, User admin) {
        Event event = findOrThrow(id);
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setEventDate(request.getEventDate());
        event.setMode(request.getMode());
        event.setLocation(request.getLocation());
        event.setCapacity(request.getCapacity());
        eventRepository.save(event);

        auditLogService.record(admin, AuditAction.UPDATE_EVENT, event.getTitle());
        return toResponse(event, null);
    }

    @Transactional
    public EventResponse adminPublish(Long id, User admin) {
        Event event = findOrThrow(id);
        event.setStatus(EventStatus.PUBLISHED);
        eventRepository.save(event);
        auditLogService.record(admin, AuditAction.UPDATE_EVENT, event.getTitle() + " — published");
        return toResponse(event, null);
    }

    @Transactional
    public EventResponse adminCancel(Long id, User admin) {
        Event event = findOrThrow(id);
        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);

        for (EventRegistration reg : registrationRepository.findByEventIdAndStatus(id, RegistrationStatus.REGISTERED)) {
            notificationService.notify(reg.getUser(), NotificationCategory.EVENT, "\"" + event.getTitle() + "\" has been cancelled.");
        }

        auditLogService.record(admin, AuditAction.UPDATE_EVENT, event.getTitle() + " — cancelled");
        return toResponse(event, null);
    }

    private EventResponse toResponse(Event event, Long viewerUserId) {
        long count = registrationRepository.countByEventIdAndStatus(event.getId(), RegistrationStatus.REGISTERED);
        boolean isRegistered = viewerUserId != null && registrationRepository.findByEventIdAndUserId(event.getId(), viewerUserId)
                .filter(r -> r.getStatus() == RegistrationStatus.REGISTERED).isPresent();
        return EventResponse.from(event, count, isRegistered);
    }

    private Event findOrThrow(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Event " + id + " not found."));
    }
}
