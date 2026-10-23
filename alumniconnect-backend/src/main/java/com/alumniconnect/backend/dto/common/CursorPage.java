package com.alumniconnect.backend.dto.common;

import java.util.List;

/**
 * A page of an "infinite" list (the feed, chat history) that is walked with an id cursor instead of a
 * page number, so new rows arriving while someone scrolls can never shift or repeat items.
 *
 * {@code nextBefore} is what the client sends back as {@code ?before=} to get the next-older page;
 * {@code null} means there is nothing older. (Plain getters instead of Lombok: a generic class.)
 */
public class CursorPage<T> {

    private final List<T> items;
    private final Long nextBefore;

    public CursorPage(List<T> items, Long nextBefore) {
        this.items = items;
        this.nextBefore = nextBefore;
    }

    public static <T> CursorPage<T> of(List<T> items, Long nextBefore) {
        return new CursorPage<>(items, nextBefore);
    }

    public List<T> getItems() {
        return items;
    }

    public Long getNextBefore() {
        return nextBefore;
    }
}
