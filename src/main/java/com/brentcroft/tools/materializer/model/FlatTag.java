package com.brentcroft.tools.materializer.model;

import com.brentcroft.tools.materializer.ValidationException;
import com.brentcroft.tools.materializer.core.OpenEvent;
import com.brentcroft.tools.materializer.core.Tag;

import static java.util.Optional.ofNullable;

public interface FlatTag< T > extends Tag< T, T >
{

    default T getItem( T t, OpenEvent openEvent )
    {
        return t;
    }

    default Object open( Object c, Object o, OpenEvent event )
    {
        T t = ( T ) c;
        T r = ( T ) o;

        try
        {
            return ofNullable( getOpener() )
                    .map( opener -> opener.open( t, r, event ) )
                    .orElse( null );
        }
        catch ( Exception e )
        {
            throw new ValidationException( this, e );
        }
    }

    default void close( Object c, Object o, String text, Object cached )
    {
        T t = ( T ) c;

        T r = ( T ) o;

        try
        {
            ofNullable( getCloser() )
                    .ifPresent( closer -> closer.close( t, r, text, cached ) );
        }
        catch ( Exception e )
        {
            throw new ValidationException( this, e );
        }

        ofNullable( getValidator() )
                .ifPresent( validator -> validator.accept( getSelf(), r ) );
    }
}
