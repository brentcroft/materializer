package com.brentcroft.tools.materializer.util.fixtures;

import com.brentcroft.tools.materializer.core.FlatTag;
import com.brentcroft.tools.materializer.core.StepTag;
import com.brentcroft.tools.materializer.core.Tag;
import com.brentcroft.tools.materializer.core.TriConsumer;
import com.brentcroft.tools.materializer.util.model.Propertied;
import lombok.Getter;
import org.xml.sax.Attributes;

import java.util.Properties;
import java.util.function.BiFunction;

import static java.util.Optional.ofNullable;

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
    ATTRIBUTE( "attribute",

            // open: cache attributes.key
            ( properties, attributes ) -> ofNullable( attributes.getValue( "key" ) )
                    .filter( v -> ! v.isEmpty() )
                    .orElseThrow( () -> new RuntimeException( "entry element has no attribute key!" ) ),

            // close: de-cache attributes.key
            ( properties, text, cache ) -> properties.setProperty( cache.toString(), text ) );

    private final String tag;
    private final StepTag< Propertied, Properties > self = this;
    private final boolean multiple = true;
    private final BiFunction< Properties, Attributes, ? > opener;
    private final TriConsumer< Properties, String, Object > closer;

    AttributePropertiesTag(
            String tag,
            BiFunction< Properties, Attributes, ? > opener,
            TriConsumer< Properties, String, Object > closer )
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