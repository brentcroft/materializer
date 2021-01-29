package com.brentcroft.tools.materializer.core;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static java.util.Objects.nonNull;

public interface Opener< A, B > extends BiFunction< A, B, Object >
{
    static < A, B > Opener< A, B > noCacheOpener( BiConsumer< A, B > simpleCloser )
    {
        return ( a, b ) -> {

            if ( nonNull( simpleCloser ) )
            {
                simpleCloser.accept( a, b );
            }

            return null;
        };
    }
}