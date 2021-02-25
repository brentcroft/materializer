package com.brentcroft.tools.materializer.model;

import com.brentcroft.tools.materializer.TagException;
import com.brentcroft.tools.materializer.ValidationException;
import com.brentcroft.tools.materializer.core.OpenEvent;
import com.brentcroft.tools.materializer.core.Tag;

import static java.util.Optional.ofNullable;

public interface StepTag< T, R > extends Tag< T, R >
{
    default boolean isStep()
    {
        return true;
    }

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
        catch ( TagException e )
        {
            throw e;
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

        try
        {
            ofNullable( getCloser() )
                    .ifPresent( closer -> closer.close( t, r, text, cached ) );
        }
        catch ( TagException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            throw new ValidationException( this, e );
        }

        ofNullable( getValidator() )
                .ifPresent( validator -> validator.accept( this, r ) );
    }
}
