package com.brentcroft.tools.materializer.core;

import org.xml.sax.Attributes;

import static java.util.Optional.ofNullable;

public interface FlatTag< T > extends Tag< T, T >
{

    default T getItem( T t )
    {
        return t;
    }

    default void open( Object o, Attributes attributes )
    {
        T r = ( T ) o;

        ofNullable( getOpener() )
                .ifPresent( opener -> opener.accept( r, attributes ) );
    }

    default void close( Object o, String text )
    {
        T r = ( T ) o;

        ofNullable( getCloser() )
                .ifPresent( closer -> closer.accept( r, text ) );

        ofNullable( getValidator() )
                .ifPresent( validator -> validator.accept( getSelf(), r ) );
    }
}
