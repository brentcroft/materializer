package com.brentcroft.tools.materializer.util.fixtures;

import com.brentcroft.tools.materializer.core.*;
import com.brentcroft.tools.materializer.util.model.Propertied;
import lombok.Getter;
import org.xml.sax.Attributes;

import java.util.Map;
import java.util.Properties;

@Getter
public enum PropertiedTag implements FlatTag< Propertied >
{
    ATTRIBUTES( "attributes", AttributePropertiesTag.ATTRIBUTE ),
    ROOT( "", ATTRIBUTES );

    private final String tag;
    private final FlatTag< Propertied > self = this;
    private final Tag< ? super Propertied, ? >[] children;

    @SafeVarargs
    PropertiedTag(
            String tag,
            Tag< ? super Propertied, ? >... children )
    {
        this.tag = tag;
        this.children = children;
    }
}

@Getter
enum AttributePropertiesTag implements StepTag< Propertied, Properties >
{
    ATTRIBUTE( "attribute", Map.class,

            // open: cache attributes.key
            ( properties, attributes ) -> Tag.getAttributesMap( attributes ),

            // close: de-cache attributes.key
            ( properties, text, cache ) -> {
                if ( ! cache.containsKey( "key" ) )
                {
                    throw new IllegalArgumentException( "missing attribute: key" );
                }
                properties.setProperty( cache.get( "key" ).toString(), text );
            } );

    private final String tag;
    private final StepTag< Propertied, Properties > self = this;
    private final boolean multiple = true;
    private final Opener< Properties, Attributes, ? > opener;
    private final Closer< Properties, String, ? > closer;

    < T > AttributePropertiesTag(
            String tag,
            Class< T > cacheClass,
            Opener< Properties, Attributes, T > opener,
            Closer< Properties, String, T > closer )
    {
        this.tag = tag;
        this.opener = opener;
        this.closer = closer;
    }

    @Override
    public Properties getItem( Propertied propertied )
    {
        return propertied.getAttributes();
    }
}