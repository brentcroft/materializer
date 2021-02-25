package com.brentcroft.tools.materializer.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TagContext
{
    private final OpenEvent event;
    private final Object cache;
    private final Tag< ?, ? > tag;
    private final TagModel< ? > model;
}
