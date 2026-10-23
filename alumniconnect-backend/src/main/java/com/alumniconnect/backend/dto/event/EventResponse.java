package com.alumniconnect.backend.dto.event;

import com.alumniconnect.backend.entity.Event;
import com.alumniconnect.backend.entity.EventMode;
import com.alumniconnect.backend.entity.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime eventDate;
    private EventMode mode;
    private String location;
    private Integer capacity;
    private long registeredCount;
    private EventStatus status;
    private boolean isRegisteredByViewer;

    public static EventResponse from(Event e, long registeredCount, boolean isRegisteredByViewer) {
        return EventResponse.builder()
                .id(e.getId())
                .title(e.getTitle())
                .description(e.getDescription())
                .eventDate(e.getEventDate())
                .mode(e.getMode())
                .location(e.getLocation())
                .capacity(e.getCapacity())
                .registeredCount(registeredCount)
                .status(e.getStatus())
                .isRegisteredByViewer(isRegisteredByViewer)
                .build();
    }
}
