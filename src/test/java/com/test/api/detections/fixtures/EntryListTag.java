package com.test.api.detections.fixtures;

import com.brentcroft.tools.materializer.core.StepTag;
import com.brentcroft.tools.materializer.core.Tag;
import com.test.api.detections.model.Attributed;
import com.test.api.detections.model.Entry;
import lombok.Getter;
import org.xml.sax.Attributes;

import java.util.List;
import java.util.function.BiConsumer;

@Getter
public enum EntryListTag implements StepTag< Attributed, List< Entry > >
{
    ENTRY_LIST(
            "attributes",
            EntryTag.ENTRY );

    private final String tag;
    private final StepTag< Attributed, List< Entry > > self = this;
    private final Tag< ?, ? >[] children;

    EntryListTag( String tag, Tag< ?, ? >... children )
    {
        this.tag = tag;
        this.children = children;
    }

    @Override
    public List< Entry > getItem( Attributed attributed )
    {
        return attributed.getAttributes();
    }
}

@Getter
enum EntryTag implements StepTag< List< Entry >, Entry >
{
    ENTRY(
            "attribute",
            ( entry, attributes ) -> entry.setKey( attributes.getValue( "key" ) ),
            Entry::setValue );

    private final String tag;
    private final StepTag< List< Entry >, Entry > self = this;
    private final BiConsumer< Entry, Attributes > opener;
    private final BiConsumer< Entry, String > closer;
    private final boolean multiple = true;

    EntryTag( String tag, BiConsumer< Entry, Attributes > opener, BiConsumer< Entry, String > closer )
    {
        this.tag = tag;
        this.opener = opener;
        this.closer = closer;
    }

    @Override
    public Entry getItem( List< Entry > entries )
    {
        Entry entry = new Entry();
        entries.add( entry );
        return entry;
    }
}
