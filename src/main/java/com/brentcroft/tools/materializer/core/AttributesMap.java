package com.brentcroft.tools.materializer.core;

import lombok.RequiredArgsConstructor;
import org.xml.sax.Attributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@RequiredArgsConstructor
public class AttributesMap
{
    private final Properties properties;

    public synchronized void forEach( BiConsumer< ? super String, ? super String > action )
    {
        properties.forEach( ( k, v ) -> action.accept( k.toString(), v.toString() ) );
    }

    public Map< String, String > asMap()
    {
        Map< String, String > map = new HashMap<>();
        forEach( map::put );
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

    public < T > T getAttribute( String key, boolean mandatory, T defaultValue, Function< String, T > getter )
    {
        if ( ! properties.containsKey( key ) )
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

        return getter.apply( properties.getProperty( key ) );
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


    /**
     * Utility method to convert Attributes to a Map.
     *
     * @param attributes XML attributes
     * @return a map of the attributes
     */
    static AttributesMap getAttributesMap( Attributes attributes )
    {
        Properties properties = new Properties();

        for ( int i = 0, n = attributes.getLength(); i < n; i++ )
        {
            String key = attributes.getLocalName( i );
            if ( key.length() == 0 )
            {
                key = attributes.getQName( i );
            }
            String value = attributes.getValue( i );
            properties.setProperty( key, value );
        }

        return new AttributesMap( properties );
    }
}
