package com.brentcroft.tools.materializer.model;

import com.brentcroft.tools.materializer.core.OpenEvent;
import com.brentcroft.tools.materializer.core.Tag;
import com.brentcroft.tools.materializer.ValidationException;

import static java.util.Optional.ofNullable;

public interface StepTag< T, R > extends Tag< T, R >
{
    default R step( Object o, OpenEvent openEvent )
    {
        return getItem( ( T ) o, openEvent );
    }

    default Object open( Object c, Object o, OpenEvent event )
    {
        T t = ( T ) c;
        R r = ( R ) o;

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
        R r = ( R ) o;

        ofNullable( getCloser() )
                .ifPresent( closer -> closer.close( t, r, text, cached ) );

        ofNullable( getValidator() )
                .ifPresent( validator -> validator.accept( getSelf(), r ) );
    }
}
