package com.brentcroft.tools.materializer.core;

import org.xml.sax.Attributes;

import static java.util.Optional.ofNullable;

public interface StepTag< T, R > extends Tag< T, R >
{

    default R step( Object o )
    {
        return getItem( ( T ) o );
    }

    default void open( Object o, Attributes attributes )
    {
        R r = ( R ) o;

        ofNullable( getOpener() )
                .ifPresent( opener -> opener.accept( r, attributes ) );
    }

    default void close( Object o, String text )
    {
        R r = ( R ) o;

        ofNullable( getCloser() )
                .ifPresent( closer -> closer.accept( r, text ) );

        ofNullable( getValidator() )
                .ifPresent( validator -> validator.accept( getSelf(), r ) );
    }
}
