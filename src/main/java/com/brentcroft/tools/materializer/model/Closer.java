package com.brentcroft.tools.materializer.model;

import com.brentcroft.tools.materializer.core.QuadConsumer;
import com.brentcroft.tools.materializer.core.TriConsumer;

import java.util.function.BiConsumer;

import static java.util.Optional.ofNullable;

public interface Closer< A, B, C, D > extends QuadConsumer< A, B, C, D >
{
    static < A, B > FlatCloser< A, B > flatCloser( BiConsumer< A, B > simpleCloser )
    {
        return ( a, b, c, d ) -> ofNullable( simpleCloser )
                .ifPresent( closer -> closer.accept( b, c ) );
    }

    static < A, B, C > FlatCacheCloser< A, B, C > flatCacheCloser( TriConsumer< A, B, C > simpleCloser )
    {
        return ( a, b, c, d ) -> ofNullable( simpleCloser )
                .ifPresent( closer -> closer.accept( b, c, d ) );
    }

    static < A, B, C > StepCloser< A, B, C > stepCloser( TriConsumer< A, B, C > simpleCloser )
    {
        return ( a, b, c, d ) -> ofNullable( simpleCloser )
                .ifPresent( closer -> closer.accept( a, b, c ) );
    }

    static < A, B, C > StepCloser< A, B, C > stepCloser( BiConsumer< B, C > simpleCloser )
    {
        return ( a, b, c, d ) -> ofNullable( simpleCloser )
                .ifPresent( closer -> closer.accept( b, c ) );
    }


    static < A, B > JumpCloser< A, B > jumpCloser( BiConsumer< A, B > simpleCloser )
    {
        return ( a, b, c, d ) -> ofNullable( simpleCloser )
                .ifPresent( closer -> closer.accept( a, b ) );
    }

    static < A, B, C, D > Closer< A, B, C, D > noCacheCloser( BiConsumer< A, B > simpleCloser )
    {
        return ( a, b, c, d ) -> ofNullable( simpleCloser )
                .ifPresent( closer -> closer.accept( a, b ) );
    }


    default void close( A a, B b, C c, Object d )
    {
        accept( a, b, c, ( D ) d );
    }
}