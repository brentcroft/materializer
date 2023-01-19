package com.brentcroft.tools.materializer.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static java.lang.String.format;

@Getter
@RequiredArgsConstructor
public class TagContext
{
    private final OpenEvent event;
    private final Object cache;
    private final Tag< ?, ? > tag;
    private final TagModel< ? > model;

    public String toString() {
        return format("%s", event.combinedTag());
    }
}
