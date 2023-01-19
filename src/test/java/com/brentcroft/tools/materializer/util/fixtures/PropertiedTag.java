package com.brentcroft.tools.materializer.util.fixtures;

import com.brentcroft.tools.materializer.core.Attribution;
import com.brentcroft.tools.materializer.core.OpenEvent;
import com.brentcroft.tools.materializer.core.Tag;
import com.brentcroft.tools.materializer.model.Closer;
import com.brentcroft.tools.materializer.model.FlatTag;
import com.brentcroft.tools.materializer.model.Opener;
import com.brentcroft.tools.materializer.model.StepTag;
import com.brentcroft.tools.materializer.util.model.Propertied;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Properties;

@Getter
public enum PropertiedTag implements FlatTag< Propertied >
{
    ATTRIBUTES( "attributes", AttributePropertiesTag.ATTRIBUTE ),
    ROOT( "", ATTRIBUTES );

    private final String tag;
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
@RequiredArgsConstructor
enum AttributePropertiesTag implements StepTag< Propertied, Properties >
{
    ATTRIBUTE(
            "attribute",
            ( propertied, properties, event ) -> event,
            ( propertied, properties, text, event ) -> properties.setProperty( event.getAttribute( "key" ), text ) );

    private final String tag;
    private final boolean multiple = true;
    private final Opener< Propertied, Properties, OpenEvent, Attribution > opener;
    private final Closer< Propertied, Properties, String, Attribution > closer;

    @Override
    public Properties getItem( Propertied propertied, OpenEvent openEvent )
    {
        return propertied.getAttributes();
    }
}