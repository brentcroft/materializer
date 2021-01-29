package com.brentcroft.tools.materializer.core;

import org.xml.sax.Attributes;

import static java.util.Optional.ofNullable;

public interface FlatTag< T > extends Tag< T, T >
{

    default T getItem( T t )
    {
        return t;
    }

    default Object open( Object o, Attributes attributes )
    {
        T r = ( T ) o;

        try
        {
            return ofNullable( getOpener() )
                    .map( opener -> opener.apply( r, attributes ) )
                    .orElse( null );
        }
        catch ( Exception e )
        {
            throw new ValidationException( this, e.getMessage() );
        }
    }

    default void close( Object o, String text, Object cached )
    {
        T r = ( T ) o;

        ofNullable( getCloser() )
                .ifPresent( closer -> closer.accept( r, text, cached ) );

        ofNullable( getValidator() )
                .ifPresent( validator -> validator.accept( getSelf(), r ) );
    }
}
