package com.test.api.fixtures;

import com.brentcroft.tools.materializer.core.Tag;
import com.test.api.model.Attributed;
import com.test.api.model.Entry;
import lombok.Getter;
import org.xml.sax.Attributes;

import java.util.function.BiConsumer;

@Getter
public enum AttributeTag implements Tag.FlatTag< Entry >
{
    ATTRIBUTE(
            "attribute",
            ( ( entry, attributes ) -> entry.setKey( attributes.getValue( "key" ) ) ),
            Entry::setValue );

    private final String tag;
    private final Tag.FlatTag< Entry > self = this;
    private final boolean multiple = true;
    private final BiConsumer< Entry, Attributes > opener;
    private final BiConsumer< Entry, String > closer;


    AttributeTag( String tag, BiConsumer< Entry, Attributes > opener, BiConsumer< Entry, String > closer )
    {
        this.tag = tag;
        this.opener = opener;
        this.closer = closer;
    }
}
