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

    interface FlatTag< T > extends Tag< T, T >
    {
        default T getItem( T t )
        {
            return t;
        }
    }

    static List< Tag< ?, ? > > fromArray( Tag< ?, ? >... tags )
    {
        return ofNullable( tags )
                .map( Arrays::asList )
                .orElse( null );
    }

    default T upcast( Object o )
    {
        return ( T ) o;
    }

    default R sidecast( Object o )
    {
        return ( R ) o;
    }

    default R upcastItem( Object o )
    {
        return getItem( upcast( o ) );
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

    default void open( Object o, Attributes attributes )
    {
        R r = sidecast( o );

        ofNullable( getOpener() )
                .ifPresent( opener -> opener.accept( r, attributes ) );
    }

    default void close( Object o, String text )
    {
        R r = sidecast( o );

        ofNullable( getCloser() )
                .ifPresent( closer -> closer.accept( r, text ) );

        ofNullable( getValidator() )
                .ifPresent( validator -> validator.accept( getSelf(), r ) );
    }

    default Iterator< Tag< ?, ? > > getIterator()
    {
        return ofNullable( getChildren() )
                .orElse( emptyList() )
                .iterator();
    }
}
