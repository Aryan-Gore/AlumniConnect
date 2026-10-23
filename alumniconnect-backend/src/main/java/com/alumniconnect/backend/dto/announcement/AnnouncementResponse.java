package com.alumniconnect.backend.dto.announcement;

import com.alumniconnect.backend.dto.common.DepartmentRef;
import com.alumniconnect.backend.entity.Announcement;
import com.alumniconnect.backend.entity.AnnouncementStatus;
import com.alumniconnect.backend.entity.Priority;
import com.alumniconnect.backend.entity.TargetAudience;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class AnnouncementResponse {
    private Long id;
    private String title;
    private String content;
    private TargetAudience targetAudience;
    private DepartmentRef targetDepartment;
    private String targetBatch;
    private Priority priority;
    private LocalDate publishDate;
    private AnnouncementStatus status;

    /** Reads the lazy target department, so call it inside a transaction. */
    public static AnnouncementResponse from(Announcement a) {
        return AnnouncementResponse.builder()
                .id(a.getId())
                .title(a.getTitle())
                .content(a.getContent())
                .targetAudience(a.getTargetAudience())
                .targetDepartment(DepartmentRef.from(a.getTargetDepartment()))
                .targetBatch(a.getTargetBatch())
                .priority(a.getPriority())
                .publishDate(a.getPublishDate())
                .status(a.getStatus())
                .build();
    }
}
