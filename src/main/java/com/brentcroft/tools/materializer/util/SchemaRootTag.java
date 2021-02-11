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

    SCHEMA(
            "schema",
            Map.class,
            ( schemaObject, attributes ) -> Tag.getAttributesMap( attributes ),
            ( schemaObject, text, cache ) -> {

                @SuppressWarnings( value = "unchecked" )
                Map< String, String > cacheMap = ( Map< String, String > ) cache;

                cacheMap
                        .forEach( ( k, v ) -> {
                            if ( k.startsWith( "xmlns:" ) )
                            {
                                schemaObject
                                        .getNamespacePrefixes()
                                        .put( k.substring( 6 ), v );
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
            SchemaReferenceTag.SIMPLE_TYPE ),

    ROOT( "", SCHEMA );

    private final String tag;
    private final boolean multiple = true;
    private final boolean choice = true;
    private final FlatTag< SchemaObject > self = this;
    private final Opener< SchemaObject, Attributes, ? > opener;
    private final Closer< SchemaObject, String, ? > closer;
    private final Tag< ? super SchemaObject, ? >[] children;

    @SafeVarargs
    SchemaRootTag(
            String tag,
            Tag< ? super SchemaObject, ? >... children
    )
    {
        this.tag = tag;
        this.opener = null;
        this.closer = null;
        this.children = children;
    }

    @SafeVarargs
    < T > SchemaRootTag(
            String tag,
            @SuppressWarnings( value = "unused" )
                    Class< T > cacheClass,
            Opener< SchemaObject, Attributes, T > opener,
            Closer< SchemaObject, String, T > closer,
            Tag< ? super SchemaObject, ? >... children
    )
    {
        this.tag = tag;
        this.opener = opener;
        this.closer = closer;
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
        this.closer = null;
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
            SchemaLeafTag.ANNOTATION )
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
            SchemaLeafTag.ANNOTATION )
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


    CHOICE( "choice", ( Tag< ? super SchemaItem, ? > ) null )
            {
                // handling forward reference
                @Override
                public Tag< ? super SchemaItem, ? >[] getChildren()
                {
                    return Tag.tags( SchemaElementTag.ELEMENT, SchemaLeafTag.SEQUENCE, SchemaLeafTag.ANNOTATION );
                }
            },

    SEQUENCE( "sequence", ( Tag< ? super SchemaItem, ? > ) null )
            {
                // handling forward reference
                @Override
                public Tag< ? super SchemaItem, ? >[] getChildren()
                {
                    return Tag.tags( SchemaElementTag.ELEMENT, SchemaLeafTag.CHOICE, SchemaLeafTag.ANNOTATION );
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
    PATTERN( "pattern" ),

    EXTENSION( "extension", ATTRIBUTE, CHOICE, SEQUENCE ),

    ANYTHING( "*" )
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
            SIMPLE_CONTENT,
            ANNOTATION );


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
