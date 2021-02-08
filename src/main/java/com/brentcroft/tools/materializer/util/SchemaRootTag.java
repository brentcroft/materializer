package com.brentcroft.tools.materializer.util;

import com.brentcroft.tools.materializer.core.*;
import lombok.Getter;
import org.xml.sax.Attributes;

import java.util.function.BiConsumer;

@Getter
public enum SchemaRootTag implements FlatTag< SchemaObject >
{
    SCHEMA(
            "schema",
            SchemaReferenceTag.ELEMENT,
            SchemaReferenceTag.COMPLEX_TYPE,
            SchemaReferenceTag.SIMPLE_TYPE ),

    ROOT( "", SCHEMA );

    private final String tag;
    private final FlatTag< SchemaObject > self = this;
    private final Tag< ? super SchemaObject, ? >[] children;

    @SafeVarargs
    SchemaRootTag(
            String tag,
            Tag< ? super SchemaObject, ? >... children
    )
    {
        this.tag = tag;
        this.children = children;
    }
}

@Getter
enum SchemaReferenceTag implements StepTag< SchemaObject, SchemaItem >
{
    ELEMENT(
            "element",
            ( item, attributes ) -> item.setAttributes( Tag.getAttributesMap( attributes ) ),
            SchemaLeafTag.COMPLEX_TYPE,
            SchemaLeafTag.SIMPLE_TYPE )
            {
                @Override
                public ElementObject getItem( SchemaObject schemaObject )
                {
                    ElementObject item = new ElementObject( null );
                    schemaObject.getRootObjects().add( item );
                    return item;
                }
            },

    COMPLEX_TYPE(
            "complexType",
            ( item, attributes ) -> item.setAttributes( Tag.getAttributesMap( attributes ) ),
            SchemaLeafTag.SEQUENCE,
            SchemaLeafTag.SIMPLE_CONTENT )
            {
                @Override
                public ComplexTypeObject getItem( SchemaObject schemaObject )
                {
                    ComplexTypeObject item = new ComplexTypeObject( null );
                    schemaObject.getComplexTypes().add( item );
                    return item;
                }
            },

    SIMPLE_TYPE(
            "simpleType",
            ( item, attributes ) -> item.setAttributes( Tag.getAttributesMap( attributes ) ),
            SchemaLeafTag.RESTRICTION )
            {
                @Override
                public SimpleTypeObject getItem( SchemaObject schemaObject )
                {
                    SimpleTypeObject item = new SimpleTypeObject( null );
                    schemaObject.getSimpleTypes().add( item );
                    return item;
                }
            };


    private final String tag;
    private final StepTag< SchemaObject, SchemaItem > self = this;
    private final boolean choice = true;
    private final Opener< SchemaItem, Attributes, ? > opener;
    private final boolean multiple = true;
    private final Tag< ? super SchemaItem, ? >[] children;

    @SafeVarargs
    SchemaReferenceTag(
            String tag,
            BiConsumer< SchemaItem, Attributes > opener,
            Tag< ? super SchemaItem, ? >... children
    )
    {
        this.tag = tag;
        this.opener = Opener.noCacheOpener( opener );
        this.children = children;
    }
}


@Getter
enum SchemaElementTag implements StepTag< SchemaItem, SchemaItem >
{
    ELEMENT(
            "element",
            ( item, attributes ) -> item.setAttributes( Tag.getAttributesMap( attributes ) ),
            SchemaLeafTag.COMPLEX_TYPE,
            SchemaLeafTag.SIMPLE_TYPE )
            {
                @Override
                public ElementObject getItem( SchemaItem schemaItem )
                {
                    ElementObject item = new ElementObject( schemaItem );
                    schemaItem.addElement( item );
                    return item;
                }
            };


    private final String tag;
    private final StepTag< SchemaItem, SchemaItem > self = this;
    private final boolean choice = true;
    private final Opener< SchemaItem, Attributes, ? > opener;
    private final boolean multiple = true;
    private final Tag< ? super SchemaItem, ? >[] children;

    @SafeVarargs
    SchemaElementTag(
            String tag,
            BiConsumer< SchemaItem, Attributes > opener,
            Tag< ? super SchemaItem, ? >... children
    )
    {
        this.tag = tag;
        this.opener = Opener.noCacheOpener( opener );
        this.children = children;
    }
}


@Getter
enum SchemaLeafTag implements FlatTag< SchemaItem >
{
    ENUMERATION( "enumeration" ),
    ATTRIBUTE( "attribute" ),
    RESTRICTION( "restriction", ENUMERATION ),
    EXTENSION( "extension", ATTRIBUTE ),

    SIMPLE_CONTENT( "simpleContent", EXTENSION ),
    SIMPLE_TYPE( "simpleType", RESTRICTION ),

    SEQUENCE( "sequence", ( Tag< ? super SchemaItem, ? > ) null )
            {
                // handling forward reference
                @Override
                public Tag< ? super SchemaItem, ? >[] getChildren()
                {
                    return Tag.tags( SchemaElementTag.ELEMENT );
                }
            },


    COMPLEX_TYPE( "complexType",
            SEQUENCE,
            SIMPLE_CONTENT );


    private final String tag;
    private final FlatTag< SchemaItem > self = this;
    private final boolean multiple = true;
    private final boolean choice = true;
    private final Opener< SchemaItem, Attributes, ? > opener;
    private final Closer< SchemaItem, String, ? > closer;
    private final Tag< ? super SchemaItem, ? >[] children;


    @SafeVarargs
    SchemaLeafTag( String tag, Tag< ? super SchemaItem, ? >... children )
    {
        this( tag, Object.class, null, null, children );
    }

    @SafeVarargs
    < C > SchemaLeafTag(
            String tag,
            Class< C > c,
            Opener< SchemaItem, Attributes, C > opener,
            Closer< SchemaItem, String, C > closer,
            Tag< ? super SchemaItem, ? >... children
    )
    {
        this.tag = tag;
        this.opener = opener;
        this.closer = closer;
        this.children = children;
    }
}
