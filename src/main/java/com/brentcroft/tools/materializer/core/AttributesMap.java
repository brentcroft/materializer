package com.brentcroft.tools.materializer.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

public class AttributesMap extends Properties
{
    public AttributesMap()
    {
        this( null );
    }

    public AttributesMap( Properties defaults )
    {
        super( defaults );
    }

    public Map< String, String > asMap()
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

    public boolean hasAttribute( String key )
    {
        return containsKey( key ) || ofNullable( defaults )
                .map( d -> ( d instanceof AttributesMap )
                           ? ( ( AttributesMap ) d ).hasAttribute( key )
                           : d.containsKey( key ) )
                .orElse( false );
    }


    public < T > T getAttribute( String key, boolean mandatory, T defaultValue, Function< String, T > getter )
    {
        if ( ! hasAttribute( key ) )
        {
            if ( mandatory )
            {
                throw new IllegalArgumentException( "Mandatory attribute is missing: " + key );
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
