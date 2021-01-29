package com.brentcroft.tools.materializer.util.fixtures;

import com.brentcroft.tools.materializer.core.FlatTag;
import com.brentcroft.tools.materializer.core.StepTag;
import com.brentcroft.tools.materializer.core.Tag;
import com.brentcroft.tools.materializer.util.model.Propertied;
import lombok.Getter;
import org.xml.sax.Attributes;

import java.util.Properties;
import java.util.function.BiConsumer;

@Getter
public enum PropertiedTag implements FlatTag< Propertied >
{
    ATTRIBUTES( "attributes",

            // open: create cache
            ( propertied, attributes ) -> propertied
                    .getAttributes()
                    .setProperty( "$currentKey", "" ),

            // close:  remove cache
            ( propertied, text ) -> propertied
                    .getAttributes()
                    .remove( "$currentKey" ),

            AttributePropertiesTag.ATTRIBUTE ),

    ROOT( "", null, null, ATTRIBUTES );


    private final String tag;
    private final FlatTag< Propertied > self = this;
    private final BiConsumer< Propertied, Attributes > opener;
    private final BiConsumer< Propertied, String > closer;
    private final Tag< ? super Propertied, ? >[] children;


    @SafeVarargs
    PropertiedTag(
            String tag,
            BiConsumer< Propertied, Attributes > opener,
            BiConsumer< Propertied, String > closer,
            Tag< ? super Propertied, ? >... children )
    {
        this.tag = tag;
        this.opener = opener;
        this.closer = closer;
        this.children = children;
    }

}

@Getter
enum AttributePropertiesTag implements StepTag< Propertied, Properties >
{
    ATTRIBUTE( "attribute",

            // open: cache attributes.key
            ( properties, attributes ) -> properties
                    .setProperty(
                            "$currentKey",
                            attributes.getValue( "key" ) ),

            // close: de-cache attributes.key
            ( properties, text ) -> properties
                    .setProperty(
                            properties.getProperty( "$currentKey" ),
                            text ) );


    private final String tag;
    private final StepTag< Propertied, Properties > self = this;
    private final boolean multiple = true;
    private final BiConsumer< Properties, Attributes > opener;
    private final BiConsumer< Properties, String > closer;


    AttributePropertiesTag(
            String tag,
            BiConsumer< Properties, Attributes > opener,
            BiConsumer< Properties, String > closer )
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