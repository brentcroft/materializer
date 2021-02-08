package com.brentcroft.tools.materializer.core;

import java.util.function.BiConsumer;

import static java.util.Optional.ofNullable;

public interface Closer< A, B, C > extends TriConsumer< A, B, C >
{
    default void close(A a, B b, Object c)
    {
        accept( a, b, (C)c );
    }

    static < A, B, C > Closer< A, B, C > noCacheCloser( BiConsumer< A, B > simpleCloser )
    {
        return ( a, b, ignored ) -> ofNullable( simpleCloser )
                .ifPresent( c -> c.accept( a, b ) );
    }
}