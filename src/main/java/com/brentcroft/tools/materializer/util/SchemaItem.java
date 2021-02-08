package com.brentcroft.tools.materializer.util;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

@Getter
public class SchemaItem
{
    private final SchemaItem parent;
    private final int index;

    private final List< ElementObject > children = new LinkedList<>();

    @Setter
    private Map< String, String > attributes;


    public SchemaItem( SchemaItem parent )
    {
        this.index = ofNullable( parent )
                .map( p -> parent.getChildren().size() )
                .orElse( 0 );
        this.parent = parent;
    }

    public void addElement( ElementObject schemaItem )
    {
        if ( this == schemaItem )
        {
            throw new RuntimeException( "Trying to add self as a child" );
        }

        children.add( schemaItem );
    }

    public String getName()
    {
        return attributes.get( "name" );
    }

    public String getTypeRef()
    {
        return attributes.get( "type" );
    }


    public boolean isOptional()
    {
        return ofNullable( attributes.get( "minOccurs" ) )
                .filter( minOccurs -> minOccurs.equals( "0" ) )
                .isPresent();
    }

    public boolean isMultiple()
    {
        return ofNullable( attributes.get( "maxOccurs" ) )
                .filter( maxOccurs -> maxOccurs.equals( "unbounded" ) )
                .isPresent();
    }

    private String depth()
    {
        return parent == null ? "  " : "  " + parent.depth();
    }


    public String toString()
    {
        return format( "%s %s",
                depth() + ( isNull( attributes ) ? "" : attributes ),
                children.size() == 0
                ? ""
                : "\n" + children
                        .stream()
                        .map( SchemaItem::toString )
                        .collect( Collectors.joining( "\n" ) )
        );
    }
}
