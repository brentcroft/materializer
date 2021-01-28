package com.brentcroft.tools.materializer.core;

import org.xml.sax.Attributes;

import java.util.function.BiConsumer;

import static java.util.Objects.isNull;

public interface Tag< T, R >
{
    String getTag();

    R getItem( T t );

    Tag< T, R > getSelf();

    default boolean isChoice()
    {
        return false;
    }

    default Tag< ? super R, ? >[] getChildren()
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

    default TagModel< ? super R > getTagModel()
    {
        return isNull( getChildren() )
               ? null
               : new TagModel<>( isChoice(), getChildren() );
    }
}
