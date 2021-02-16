package com.brentcroft.tools.materializer.util;

import com.brentcroft.tools.materializer.Materializer;
import com.brentcroft.tools.materializer.core.*;
import lombok.Getter;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

@Getter
public enum SchemaRootTag implements FlatTag< SchemaObject >
{
    ANYTHING( "*" )
            {
                @Override
                public Tag< ? super SchemaObject, ? >[] getChildren()
                {
                    return Tag.tags( ANYTHING );
                }
            },


    IMPORT(
            "import",
            Map.class,
            ( schemaObject, attributes ) -> Tag.getAttributesMap( attributes )
    )
            {
                @Override
                public Closer< SchemaObject, String, Map< ?, ? > > getCloser()
                {

                    return ( schemaObject, text, cache ) -> {

                        File currentLocus = new File( schemaObject.getSystemId() );

                        String systemId = format( "%s/%s", currentLocus.getParent(), cache.get( "schemaLocation" ) );

                        Materializer< SchemaObject > materializer = new Materializer<>(
                                () -> SchemaRootTag.ROOT,
                                () -> {
                                    SchemaObject so = new SchemaObject();
                                    so.setSystemId( systemId );
                                    return so;
                                } );

                        try
                        {

                            SchemaObject importedSchemaObject = materializer
                                    .apply( new InputSource( new FileInputStream( systemId ) ) );

                            importedSchemaObject.reify();

                            schemaObject.getRootObjects().addAll( importedSchemaObject.getRootObjects() );
                            schemaObject.getComplexTypes().addAll( importedSchemaObject.getComplexTypes() );
                            schemaObject.getSimpleTypes().addAll( importedSchemaObject.getSimpleTypes() );

                        }
                        catch ( FileNotFoundException e )
                        {
                            throw new RuntimeException( e );
                        }

                    };
                }
            },

    ATTRIBUTE( "attribute", ANYTHING ),
    ATTRIBUTEGROUP( "attributeGroup", ANYTHING ),

    SCHEMA(
            "schema",
            ( schemaObject, attributes ) -> {

                Map< String, String > cacheMap = Tag.getAttributesMap( attributes );

                cacheMap
                        .forEach( ( k, v ) -> {
                            if ( k.startsWith( "xmlns:" ) )
                            {
                                String prefix = k.substring( 6 );

                                schemaObject
                                        .getNamespacePrefixes()
                                        .put( prefix, v );

                                if ( "http://www.w3.org/2001/XMLSchema".equals( v ) )
                                {
                                    schemaObject.setXsdPrefix( prefix );
                                }
                            }
                        } );

                ofNullable( cacheMap.get( "targetNamespace" ) )
                        .flatMap( targetNamespace -> schemaObject
                                .getNamespacePrefixes()
                                .entrySet()
                                .stream()
                                .filter( entry -> entry.getValue().equals( targetNamespace ) )
                                .findAny()
                                .map( Map.Entry::getKey ) )
                        .ifPresent( schemaObject::setLocalPrefix );
            },
            IMPORT,
            SchemaReferenceTag.ELEMENT,
            SchemaReferenceTag.COMPLEX_TYPE,
            SchemaReferenceTag.SIMPLE_TYPE,
            ATTRIBUTE, ATTRIBUTEGROUP ),

    ROOT( "", SCHEMA );

    private final String tag;
    private final boolean multiple = true;
    private final boolean choice = true;
    private final FlatTag< SchemaObject > self = this;
    private final Opener< SchemaObject, Attributes, ? > opener;
    private final Tag< ? super SchemaObject, ? >[] children;

    @SafeVarargs
    SchemaRootTag(
            String tag,
            Tag< ? super SchemaObject, ? >... children
    )
    {
        this.tag = tag;
        this.opener = null;
        this.children = children;
    }

    @SafeVarargs
    SchemaRootTag(
            String tag,
            BiConsumer< SchemaObject, Attributes > opener,
            Tag< ? super SchemaObject, ? >... children
    )
    {
        this.tag = tag;
        this.opener = Opener.noCacheOpener( opener );
        this.children = children;
    }

    < T > SchemaRootTag(
            String tag,
            @SuppressWarnings( value = "unused" )
                    Class< T > cacheClass,
            Opener< SchemaObject, Attributes, T > opener
    )
    {
        this.tag = tag;
        this.opener = opener;
        this.children = Tag.tags();
    }
}

@Getter
enum SchemaReferenceTag implements StepTag< SchemaObject, SchemaItem >
{
    ELEMENT(
            "element",
            ( item, attributes ) -> item.setAttributes( Tag.getAttributesMap( attributes ) ),
            SchemaLeafTag.COMPLEX_TYPE,
            SchemaLeafTag.SIMPLE_TYPE,
            SchemaLeafTag.ANNOTATION )
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
            SchemaLeafTag.CHOICE,
            SchemaLeafTag.SEQUENCE,
            SchemaLeafTag.SIMPLE_CONTENT,
            SchemaLeafTag.ANNOTATION,
            SchemaLeafTag.ANYATTRIBUTE )
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
            SchemaLeafTag.RESTRICTION,
            SchemaLeafTag.ANNOTATION,
            SchemaLeafTag.LIST )
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
            SchemaLeafTag.SIMPLE_TYPE,
            SchemaLeafTag.ANNOTATION )
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
    ANYTHING( "*" )
            {
                @Override
                public Tag< ? super SchemaItem, ? >[] getChildren()
                {
                    return Tag.tags( ANYTHING );
                }
            },

    CHOICE( "choice", ( Tag< ? super SchemaItem, ? > ) null )
            {
                // handling forward reference
                @Override
                public Tag< ? super SchemaItem, ? >[] getChildren()
                {
                    return Tag.tags( SchemaElementTag.ELEMENT, SchemaLeafTag.SEQUENCE, SchemaLeafTag.ANNOTATION, SchemaLeafTag.ANY );
                }
            },

    SEQUENCE( "sequence", ( Tag< ? super SchemaItem, ? > ) null )
            {
                // handling forward reference
                @Override
                public Tag< ? super SchemaItem, ? >[] getChildren()
                {
                    return Tag.tags( SchemaElementTag.ELEMENT, SchemaLeafTag.CHOICE, SchemaLeafTag.ANNOTATION, SchemaLeafTag.ANY );
                }
            },


    LENGTH( "length" ),
    FRACTIONDIGITS( "fractionDigits" ),
    TOTALDIGITS( "totalDigits" ),
    MININCLUSIVE( "minInclusive" ),
    MAXINCLUSIVE( "maxInclusive" ),
    MINLENGTH( "minLength" ),
    MAXLENGTH( "maxLength" ),
    ATTRIBUTE( "attribute" ),
    ANYATTRIBUTE( "anyAttribute", ANYTHING ),
    ATTRIBUTEGROUP( "attributeGroup" ),
    PATTERN( "pattern" ),
    LIST( "list" ),

    EXTENSION( "extension", ATTRIBUTE, ANYATTRIBUTE, CHOICE, SEQUENCE ),


    ANY( "any" )
            {
                @Override
                public Tag< ? super SchemaItem, ? >[] getChildren()
                {
                    return Tag.tags( ANYTHING );
                }
            },

    DOCUMENTATION( "documentation", ANYTHING ),
    ANNOTATION( "annotation", DOCUMENTATION ),

    ENUMERATION( "enumeration" ),
    RESTRICTION( "restriction", PATTERN, ENUMERATION, MINLENGTH, MAXLENGTH, TOTALDIGITS, LENGTH, MININCLUSIVE, MAXINCLUSIVE, FRACTIONDIGITS ),

    SIMPLE_CONTENT( "simpleContent", EXTENSION, ANNOTATION ),
    SIMPLE_TYPE( "simpleType", RESTRICTION, ANNOTATION ),


    COMPLEX_TYPE( "complexType",
            SEQUENCE,
            CHOICE,
            SIMPLE_CONTENT,
            ANNOTATION,
            ANYATTRIBUTE );


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
            @SuppressWarnings( value = "unused" )
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
