package com.alumniconnect.backend.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/** The "profile 70% complete" bar: an overall percentage plus the checklist behind it. */
@Getter
@Builder
@AllArgsConstructor
public class ProfileCompletionResponse {
    private int percent;
    private List<Item> items;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Item {
        private String key;
        private String label;
        /** What to do when the item is still missing, e.g. "Upload a clear headshot". */
        private String hint;
        private int points;
        private boolean done;
    }
}
