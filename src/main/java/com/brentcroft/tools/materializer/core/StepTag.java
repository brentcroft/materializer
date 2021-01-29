package com.brentcroft.tools.materializer.core;

import org.xml.sax.Attributes;

import static java.util.Optional.ofNullable;

public interface StepTag< T, R > extends Tag< T, R >
{

    default R step( Object o )
    {
        return getItem( ( T ) o );
    }

    default Object open( Object o, Attributes attributes )
    {
        R r = ( R ) o;

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
        R r = ( R ) o;

        ofNullable( getCloser() )
                .ifPresent( closer -> closer.accept( r, text, cached ) );

        ofNullable( getValidator() )
                .ifPresent( validator -> validator.accept( getSelf(), r ) );
    }
}
