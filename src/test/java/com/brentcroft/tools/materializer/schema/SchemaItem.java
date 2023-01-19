package com.brentcroft.tools.materializer.schema;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

@Getter
public class SchemaItem
{
    private final SchemaItem parent;
    private final int index;

    private final List< ElementObject > children = new LinkedList<>();
    private SchemaItem reified;


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


    public void reify( SchemaObject schemaObject )
    {
        if ( isNull( reified ) )
        {
            if ( getChildren().isEmpty() )
            {
                if ( nonNull( getRef() ) )
                {
                    String localRef = schemaObject.localName( getRef() );

                    reified = schemaObject
                            .getRootObjects()
                            .stream()
                            .filter( ro -> ro.getName().equals( localRef ) )
                            .map( ro -> ( SchemaItem ) ro )
                            .findAny()
                            .orElse( null );

                    if ( isNull( reified ) )
                    {
                        throw new IllegalArgumentException( format( "Un-reified item ref: %s; %s", this, schemaObject.getSystemId() ) );
                    }
                }
                else if ( nonNull( getTypeRef() ) )
                {
                    // reference to primitive
                    if ( getTypeRef().startsWith( schemaObject.getXsdPrefix() + ":" ) )
                    {
                        reified = this;
                    }
                    else
                    {
                        String localName = schemaObject.localName( getTypeRef() );

                        reified = schemaObject
                                .getComplexTypes()
                                .stream()
                                .filter( ct -> ct.getName().equals( localName ) )
                                .map( ct -> ( SchemaItem ) ct )
                                .findAny()
                                .orElseGet( () -> schemaObject
                                        .getSimpleTypes()
                                        .stream()
                                        .filter( ct -> ct.getName().equals( localName ) )
                                        .findAny()
                                        .orElse( null ) );

                        if ( isNull( reified ) )
                        {
                            throw new IllegalArgumentException( format( "Un-reified type ref: %s; %s", this, schemaObject.getSystemId() ) );
                        }
                    }
                }
                else
                {
                    // TODO: reify simple types
                    reified = this;
                }
            }
            else
            {
                for ( SchemaItem item : getChildren() )
                {
                    item.reify( schemaObject );
                }
                reified = this;
            }
        }
    }


    public String getName()
    {
        return attributes.get( "name" );
    }

    public String getTypeRef()
    {
        return attributes.get( "type" );
    }

    public String getRef()
    {
        return attributes.get( "ref" );
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
