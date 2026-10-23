package com.alumniconnect.backend.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** Headline numbers for the public landing page. Counts only — nothing here identifies a person. */
@Getter
@Builder
@AllArgsConstructor
public class PublicStatsResponse {

    /** Alumni in the registry (imported by the office, claimed or not). */
    private long alumniRecords;
    /** Alumni who have claimed their profile and can sign in. */
    private long alumniMembers;
    private long studentMembers;
    private long institutes;
    private long departments;
    private long upcomingEvents;
    private long openJobs;
}
