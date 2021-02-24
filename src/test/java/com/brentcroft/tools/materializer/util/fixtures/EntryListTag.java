package com.brentcroft.tools.materializer.util.fixtures;

import com.brentcroft.tools.materializer.core.*;
import com.brentcroft.tools.materializer.model.*;
import com.brentcroft.tools.materializer.util.model.Attributed;
import com.brentcroft.tools.materializer.util.model.Entry;
import lombok.Getter;

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
    private final Tag< ? super List< Entry >, ? >[] children;

    @SafeVarargs
    EntryListTag( String tag, Tag< ? super List< Entry >, ? >... children )
    {
        this.tag = tag;
        this.children = children;
    }

    @Override
    public List< Entry > getItem( Attributed attributed, OpenEvent openEvent )
    {
        return attributed.getAttributes();
    }
}

@Getter
enum EntryTag implements StepTag< List< Entry >, Entry >
{
    ENTRY(
            "attribute",
            ( entry, event ) -> entry.setKey( event.getAttribute( "key" ) ),
            Entry::setValue );

    private final String tag;
    private final StepTag< List< Entry >, Entry > self = this;
    private final StepOpener< List< Entry >, Entry, OpenEvent > opener;
    private final StepCloser< List< Entry >, Entry, String > closer;
    private final boolean multiple = true;

    EntryTag( String tag, BiConsumer< Entry, OpenEvent > opener, BiConsumer< Entry, String > closer )
    {
        this.tag = tag;
        this.opener = Opener.stepOpener( opener );
        this.closer = Closer.stepCloser( closer );
    }

    @Override
    public Entry getItem( List< Entry > entries, OpenEvent openEvent )
    {
        Entry entry = new Entry();
        entries.add( entry );
        return entry;
    }
}
