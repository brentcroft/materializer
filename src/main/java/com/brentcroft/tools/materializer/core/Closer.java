package com.brentcroft.tools.materializer.core;

import java.util.function.BiConsumer;

import static java.util.Optional.ofNullable;

public interface Closer< A, B > extends TriConsumer< A, B, Object >
{
    static < A, B > Closer< A, B > noCacheCloser( BiConsumer< A, B > simpleCloser )
    {
        return ( a, b, ignored ) -> ofNullable( simpleCloser )
                .ifPresent( c -> c.accept( a, b ) );
    }
}