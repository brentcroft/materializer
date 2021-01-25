package com.test.api.fixtures;

import com.brentcroft.tools.materializer.core.Tag;
import com.test.api.model.Attributed;
import com.test.api.model.Entry;
import lombok.Getter;

import java.util.List;

@Getter
public enum EntryListTag implements Tag< Attributed, Entry >
{
    ATTRIBUTES(
            "attributes",
            AttributeTag.ATTRIBUTE );

    private final String tag;
    private final Tag< Attributed, Entry > self = this;
    private final List< Tag< ?, ? > > children;


    EntryListTag( String tag, Tag< ?, ? >... children )
    {
        this.tag = tag;
        this.children = Tag.fromArray( children );
    }

    @Override
    public Entry getItem( Attributed attributed )
    {
        Entry entry = new Entry();
        attributed.getAttributes().add( entry );
        return entry;
    }
}
