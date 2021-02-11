package com.brentcroft.tools.materializer.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
enum TagType
{
    FLAT( "Flat" ),
    STEP( "Step" );
    private final String type;
}
