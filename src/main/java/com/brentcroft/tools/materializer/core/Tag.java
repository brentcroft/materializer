package com.brentcroft.tools.materializer.core;

import org.xml.sax.Attributes;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

public interface Tag< T, R >
{
    String getTag();

    R getItem( T t );

    Tag< T, R > getSelf();

    static List< Tag< ?, ? > > fromArray( Tag< ?, ? >... tags )
    {
        return ofNullable( tags )
                .map( Arrays::asList )
                .orElse( null );
    }

    default List< Tag< ?, ? > > getChildren()
    {
        return null;
    }

    default boolean isOptional()
    {
        return false;
    }

    default boolean isMultiple()
    {
        return false;
    }

    default BiConsumer< R, Attributes > getOpener()
    {
        return null;
    }

    default BiConsumer< R, String > getCloser()
    {
        return null;
    }

    default BiConsumer< Tag< T, R >, R > getValidator()
    {
        return null;
    }

    void open( Object o, Attributes attributes );

    void close( Object o, String text );

    default Iterator< Tag< ?, ? > > getIterator()
    {
        return ofNullable( getChildren() )
                .orElse( emptyList() )
                .iterator();
    }
}
