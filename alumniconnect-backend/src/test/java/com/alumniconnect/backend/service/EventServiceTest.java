package com.alumniconnect.backend.service;

import com.alumniconnect.backend.entity.*;
import com.alumniconnect.backend.exception.BadRequestException;
import com.alumniconnect.backend.exception.DuplicateResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock private com.alumniconnect.backend.repository.EventRepository eventRepository;
    @Mock private com.alumniconnect.backend.repository.EventRegistrationRepository registrationRepository;
    @Mock private com.alumniconnect.backend.repository.UserRepository userRepository;
    @Mock private AuditLogService auditLogService;
    @Mock private NotificationService notificationService;

    private EventService eventService;

    @BeforeEach
    void setUp() {
        eventService = new EventService(eventRepository, registrationRepository, userRepository,
                auditLogService, notificationService);
    }

    private Event publishedEvent(int capacity) {
        return Event.builder()
                .id(1L).title("Test Event").capacity(capacity)
                .eventDate(LocalDateTime.now().plusDays(7))
                .mode(EventMode.ONLINE).status(EventStatus.PUBLISHED)
                .build();
    }

    @Test
    void register_rejectsOnceCapacityIsReached() {
        Event event = publishedEvent(2);
        User user = User.builder().id(10L).fullName("A Student").build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(registrationRepository.findByEventIdAndUserId(1L, 10L)).thenReturn(Optional.empty());
        when(registrationRepository.countByEventIdAndStatus(1L, RegistrationStatus.REGISTERED)).thenReturn(2L); // already full

        assertThatThrownBy(() -> eventService.register(1L, user))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("capacity");

        verify(registrationRepository, never()).save(any());
    }

    @Test
    void register_rejectsADuplicateActiveRegistration() {
        Event event = publishedEvent(50);
        User user = User.builder().id(10L).fullName("A Student").build();
        EventRegistration existing = EventRegistration.builder()
                .event(event).user(user).status(RegistrationStatus.REGISTERED).build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(registrationRepository.findByEventIdAndUserId(1L, 10L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> eventService.register(1L, user))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void register_rejectsRegisteringForANonPublishedEvent() {
        Event draftEvent = publishedEvent(50);
        draftEvent.setStatus(EventStatus.DRAFT);
        User user = User.builder().id(10L).build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(draftEvent));

        assertThatThrownBy(() -> eventService.register(1L, user))
                .isInstanceOf(BadRequestException.class);

        verify(registrationRepository, never()).save(any());
    }

    @Test
    void register_reactivatesAPreviouslyCancelledRegistrationInsteadOfCreatingASecondRow() {
        Event event = publishedEvent(50);
        User user = User.builder().id(10L).build();
        EventRegistration cancelled = EventRegistration.builder()
                .event(event).user(user).status(RegistrationStatus.CANCELLED).build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(registrationRepository.findByEventIdAndUserId(1L, 10L)).thenReturn(Optional.of(cancelled));
        when(registrationRepository.countByEventIdAndStatus(1L, RegistrationStatus.REGISTERED)).thenReturn(0L);

        eventService.register(1L, user);

        // The existing (previously-cancelled) row is flipped and saved —
        // never a brand new EventRegistration for the same event+user.
        assertThat(cancelled.getStatus()).isEqualTo(RegistrationStatus.REGISTERED);
        verify(registrationRepository, times(1)).save(cancelled);
        verify(registrationRepository, never()).save(argThat(r -> r != cancelled));
    }

    @Test
    void cancelRegistration_marksExistingRegistrationCancelledRatherThanDeletingIt() {
        Event event = publishedEvent(50);
        User user = User.builder().id(10L).build();
        EventRegistration registration = EventRegistration.builder()
                .event(event).user(user).status(RegistrationStatus.REGISTERED).build();

        when(registrationRepository.findByEventIdAndUserId(1L, 10L)).thenReturn(Optional.of(registration));

        eventService.cancelRegistration(1L, 10L);

        assertThat(registration.getStatus()).isEqualTo(RegistrationStatus.CANCELLED);
        verify(registrationRepository, never()).delete(any());
        verify(registrationRepository, never()).deleteById(any());
    }
}
