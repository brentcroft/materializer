package com.brentcroft.tools.materializer.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.xml.sax.Attributes;

@Getter
@RequiredArgsConstructor
public class OpenEvent
{
    private final String uri;
    private final String localName;
    private final String qName;
    private final Attributes attributes;

    @Setter
    private Tag< ?, ? > tag;

    public AttributesMap getAttributesMap()
    {
        return AttributesMap.getAttributesMap( attributes );
    }
}
