package com.brentcroft.tools.materializer.core;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static java.util.Objects.nonNull;

public interface Opener< A, B, C, D > extends TriFunction< A, B, C, D >
{
    /**
     * Ignores any context object and caches an intermediate object.
     *
     * @param opener a {@literal BiFunction< A, B, C >}
     * @param <A>    a Type of item
     * @param <B>    a Type of OpenEvent
     * @param <C>    a Type of object to cache
     * @return a {@literal FlatCacheOpener< A, B, C >}
     */
    static < A, B, C > FlatCacheOpener< A, B, C > flatCacheOpener( BiFunction< A, B, C > opener )
    {
        return ( a, b, c ) -> {

            if ( nonNull( opener ) )
            {
                return opener.apply( b, c );
            }

            return null;
        };
    }

    /**
     * Ignores any context object and doesn't cache an intermediate object.
     *
     * @param opener a {@literal BiConsumer< A, B >}
     * @param <A>    a Type of item
     * @param <B>    a Type of attributes
     * @return a {@literal FlatOpener< A, B >}
     */
    static < A, B > FlatOpener< A, B > flatOpener( BiConsumer< A, B > opener )
    {
        return ( a, b, c ) -> {

            if ( nonNull( opener ) )
            {
                opener.accept( b, c );
            }

            return null;
        };
    }


    /**
     * Doesn't cache an intermediate object.
     *
     * @param opener a {@literal TriConsumer< A, B, C >}
     * @param <A>    a Type of context object
     * @param <B>    a Type of item
     * @param <C>    a Type of attributes
     * @return a {@literal StepOpener< A, B, C >}
     */
    static < A, B, C > StepOpener< A, B, C > stepOpener( TriConsumer< A, B, C > opener )
    {
        return ( a, b, c ) -> {

            if ( nonNull( opener ) )
            {
                opener.accept( a, b, c );
            }

            return null;
        };
    }


    /**
     * Ignores any context object and doesn't cache an intermediate object.
     *
     * @param opener a {@literal BiConsumer< B,C>}
     * @param <A>    a Type of context object
     * @param <B>    a Type of item
     * @param <C>    a Type of attributes
     * @return a {@literal StepOpener< A, B, C >}
     */
    static < A, B, C > StepOpener< A, B, C > stepOpener( BiConsumer< B, C > opener )
    {
        return ( a, b, c ) -> {

            if ( nonNull( opener ) )
            {
                opener.accept( b, c );
            }

            return null;
        };
    }


    default D open( A context, B item, C event )
    {
        return apply( context, item, event );
    }
}