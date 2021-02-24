package com.brentcroft.tools.materializer.core;

import com.brentcroft.tools.materializer.ContextValue;
import lombok.Getter;
import lombok.Setter;
import org.xml.sax.Attributes;

import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

@Getter
public class OpenEvent extends AttributesMap
{
    private final String uri;
    private final String localName;
    private final String qName;
    private final ContextValue contextValue;

    @Setter
    private Tag< ?, ? > tag;

    public OpenEvent( String uri, String localName, String qName, Attributes attributes, ContextValue contextValue )
    {
        this.uri = uri;
        this.localName = localName;
        this.qName = qName;
        this.contextValue = contextValue;

        if ( nonNull( attributes ) )
        {
            harvestAttributes( attributes );
        }
    }

    private void harvestAttributes( Attributes attributes )
    {
        for ( int i = 0, n = attributes.getLength(); i < n; i++ )
        {
            String[] key = {attributes.getLocalName( i )};

            if ( key[ 0 ].length() == 0 )
            {
                key[ 0 ] = attributes.getQName( i );
            }

            String value = attributes.getValue( i );

            setProperty(
                    key[ 0 ],
                    ofNullable( contextValue )
                            .map( cv -> cv.map( key[ 0 ], value ) )
                            .orElse( value ) );
        }
    }

    public String combinedTag()
    {
        return isNull( localName ) || localName.isEmpty()
               ? qName
               : isNull( qName ) || qName.isEmpty() || localName.equals( qName )
                 ? localName
                 : format( "%s | %s", localName, qName );
    }


    public String getTagHead()
    {
        return format(
                "%s%s",
                combinedTag(),
                asMap()
                        .entrySet()
                        .stream()
                        .map( entry -> format( " %s=\"%s\"", entry.getKey(), entry.getValue() ) )
                        .collect( Collectors.joining() )
        );
    }

    public void putContextValue( String key, Object value )
    {
        if ( nonNull( contextValue ) )
        {
            contextValue.put( key, value );
        }
    }

    public ContextValue inContext()
    {
        return nonNull( contextValue )
               ? contextValue.inContext()
               : null;
    }
}
