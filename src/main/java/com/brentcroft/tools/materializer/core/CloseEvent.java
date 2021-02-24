package com.brentcroft.tools.materializer.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.xml.sax.Attributes;

@Getter
@RequiredArgsConstructor
public class CloseEvent
{
    private final OpenEvent openEvent;

}
