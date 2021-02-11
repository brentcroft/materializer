package com.brentcroft.tools.materializer.core;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static java.util.Objects.nonNull;

public interface Opener< A, B, C > extends BiFunction< A, B, C >
{
    static < A, B, C > Opener< A, B, C > noCacheOpener( BiConsumer< A, B > opener )
    {
        return ( a, b ) -> {

            if ( nonNull( opener ) )
            {
                opener.accept( a, b );
            }

            return null;
        };
    }

    default C open( A a, B b )
    {
        return apply( a, b );
    }
}