package com.test.api.fixtures;

import com.brentcroft.tools.materializer.core.Tag;
import com.test.api.model.Detection;
import com.test.api.model.Entry;
import lombok.Getter;
import org.xml.sax.Attributes;

import java.util.List;
import java.util.function.BiConsumer;

@Getter
public enum EntryTag implements Tag< Detection, Entry >
{
    ENTRY(
            "attribute",
            ( entry, attributes ) -> entry.setKey( attributes.getValue( "key" ) ),
            Entry::setValue );

    private final String tag;
    private final Tag< Detection, Entry > self = this;
    private final List< Tag< ?, ? > > children;
    private final BiConsumer< Entry, Attributes > opener;
    private final BiConsumer< Entry, String > closer;

    EntryTag( String tag, BiConsumer< Entry, Attributes > opener, BiConsumer< Entry, String > closer, Tag< ?, ? >... children )
    {
        this.tag = tag;
        this.opener = opener;
        this.closer = closer;
        this.children = Tag.fromArray( children );
    }

    @Override
    public Entry getItem( Detection detection )
    {
        return new Entry();
    }
}
