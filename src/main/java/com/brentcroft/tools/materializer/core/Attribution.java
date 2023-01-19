package com.brentcroft.tools.materializer.core;

import com.brentcroft.tools.materializer.TagException;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

public class Attribution extends Properties
{
    public Attribution()
    {
        this( null );
    }

    public Attribution( Properties defaults )
    {
        super( defaults );
    }

    public Map< String, Object > asMap()
    {
        Map< String, Object > map = new HashMap<>();
        forEach( ( k, v ) -> map.put( k.toString(), v ) );
        return map;
    }

    public Map< String, String > asStringMap()
    {
        Map< String, String > map = new HashMap<>();
        forEach( ( k, v ) -> map.put( k.toString(), v.toString() ) );
        return map;
    }

    public < T > T applyAttribute( String key, boolean mandatory, T defaultValue, Function< String, T > getter, Consumer< T > setter )
    {
        T t = getAttribute( key, mandatory, defaultValue, getter );

        if ( nonNull( t ) || mandatory )
        {
            setter.accept( t );
        }

        return t;
    }


    /**
     * If the attribute <code>key</code> is available then call and return the supplied Caller,
     * otherwise return null.
     *
     * @param key    the attribute that must be available
     * @param caller a Caller to call if the attribute is available.
     * @param <V>    The return type of a call to the Caller
     * @return The value returned from the Caller or null
     */
    public < V > V onHasAttribute( String key, Callable< V > caller )
    {
        try
        {
            return hasAttribute( key ) ? caller.call() : null;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    /**
     * If the attribute <code>key</code> is available,
     * and the Supplier returns <code>true</code>,
     * then call and return the supplied Caller,
     * otherwise return null.
     *
     * @param key    the attribute that must be available
     * @param and    a boolean Supplier that must return true
     * @param caller a Caller to call if the attribute is available and the Supplier returns true
     * @param <V>    The return type of a call to the Caller
     * @return The value returned from the Caller or null
     */
    public < V > V onHasAttributeAnd( String key, Supplier< Boolean > and, Callable< V > caller )
    {
        try
        {
            return hasAttribute( key ) && and.get() ? caller.call() : null;
        }
        catch ( TagException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    public boolean hasAttribute( String key )
    {
        return containsKey( key ) || ofNullable( defaults )
                .map( d -> ( d instanceof Attribution )
                           ? ( ( Attribution ) d ).hasAttribute( key )
                           : d.containsKey( key ) )
                .orElse( false );
    }


    public < T > T getAttribute( String key, boolean mandatory, T defaultValue, Function< String, T > getter )
    {
        if ( ! hasAttribute( key ) )
        {
            if ( mandatory )
            {
                throw new RuntimeException( "Mandatory attribute is missing: " + key );
            }

            return defaultValue;
        }
        else if ( isNull( getter ) )
        {
            return defaultValue;
        }

        return getter.apply( getProperty( key ) );
    }


    public < T > T getAttribute( String key, T defaultValue )
    {
        return getAttribute( key, false, defaultValue, null );
    }

    public < T > T applyAttribute( String key, boolean mandatory, Function< String, T > getter, Consumer< T > setter )
    {
        return applyAttribute( key, mandatory, null, getter, setter );
    }

    public < T > T getAttribute( String key, boolean mandatory, Function< String, T > getter )
    {
        return getAttribute( key, mandatory, null, getter );
    }

    public < T > T applyAttribute( String key, Function< String, T > getter, Consumer< T > setter )
    {
        return applyAttribute( key, true, null, getter, setter );
    }

    public < T > T getAttribute( String key, Function< String, T > getter )
    {
        return getAttribute( key, true, null, getter );
    }

    public < T > T applyAttribute( String key, T defaultValue, Consumer< T > setter )
    {
        return applyAttribute( key, false, defaultValue, null, setter );
    }

    public < T > T applyAttribute( String key, T defaultValue, Function< String, T > getter, Consumer< T > setter )
    {
        return applyAttribute( key, false, defaultValue, getter, setter );
    }

    public String applyAttribute( String key, String defaultValue, Consumer< String > setter )
    {
        return applyAttribute( key, false, defaultValue, String::valueOf, setter );
    }

    public String getAttribute( String key, String defaultValue )
    {
        return getAttribute( key, false, defaultValue, String::valueOf );
    }

    public String applyAttribute( String key, boolean mandatory, Consumer< String > setter )
    {
        return applyAttribute( key, mandatory, null, String::valueOf, setter );
    }

    public String getAttribute( String key, boolean mandatory )
    {
        return getAttribute( key, mandatory, null, String::valueOf );
    }

    public String applyAttribute( String key, Consumer< String > setter )
    {
        return applyAttribute( key, true, null, String::valueOf, setter );
    }

    public String getAttribute( String key )
    {
        return getAttribute( key, true, null, String::valueOf );
    }
}
