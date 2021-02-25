package com.brentcroft.tools.materializer.util;

import com.brentcroft.tools.materializer.Materializer;
import com.brentcroft.tools.materializer.core.OpenEvent;
import com.brentcroft.tools.materializer.core.Tag;
import com.brentcroft.tools.materializer.core.TriConsumer;
import com.brentcroft.tools.materializer.model.*;
import lombok.Getter;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

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
            ( schemaObject, event ) -> event )
            {
                @Override
                public Closer< SchemaObject, SchemaObject, String, OpenEvent > getCloser()
                {

                    return ( context, schemaObject, text, event ) -> {

                        File currentLocus = new File( schemaObject.getSystemId() );

                        String systemId = format( "%s/%s", currentLocus.getParent(), event.getAttribute( "schemaLocation" ) );

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
            ( schemaObject, event ) -> {

                event
                        .forEach( ( k, v ) -> {
                            if ( k.toString().startsWith( "xmlns:" ) )
                            {
                                String prefix = k.toString().substring( 6 );

                                schemaObject
                                        .getNamespacePrefixes()
                                        .put( prefix, v.toString() );

                                if ( "http://www.w3.org/2001/XMLSchema".equals( v ) )
                                {
                                    schemaObject.setXsdPrefix( prefix );
                                }
                            }
                        } );

                ofNullable( event.getAttribute( "targetNamespace", false ) )
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
    private final Opener< SchemaObject, SchemaObject, OpenEvent, ? > opener;
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
            BiConsumer< SchemaObject, OpenEvent > opener,
            Tag< ? super SchemaObject, ? >... children
    )
    {
        this.tag = tag;
        this.opener = Opener.flatOpener( opener );
        this.children = children;
    }

    < T > SchemaRootTag(
            String tag,
            BiFunction< SchemaObject, OpenEvent, T > opener
    )
    {
        this.tag = tag;
        this.opener = Opener.flatCacheOpener( opener );
        this.children = Tag.tags();
    }
}

@Getter
enum SchemaReferenceTag implements StepTag< SchemaObject, SchemaItem >
{
    ELEMENT(
            "element",
            ( schema, item, event ) -> item.setAttributes( event.asStringMap() ),
            SchemaLeafTag.COMPLEX_TYPE,
            SchemaLeafTag.SIMPLE_TYPE,
            SchemaLeafTag.ANNOTATION )
            {
                @Override
                public ElementObject getItem( SchemaObject schemaObject, OpenEvent event )
                {
                    ElementObject item = new ElementObject( null );
                    schemaObject.getRootObjects().add( item );
                    return item;
                }
            },

    COMPLEX_TYPE(
            "complexType",
            ( schema, item, event ) -> item.setAttributes( event.asStringMap() ),
            SchemaLeafTag.CHOICE,
            SchemaLeafTag.SEQUENCE,
            SchemaLeafTag.SIMPLE_CONTENT,
            SchemaLeafTag.ANNOTATION,
            SchemaLeafTag.ANYATTRIBUTE )
            {
                @Override
                public ComplexTypeObject getItem( SchemaObject schemaObject, OpenEvent event )
                {
                    ComplexTypeObject item = new ComplexTypeObject( null );
                    schemaObject.getComplexTypes().add( item );
                    return item;
                }
            },

    SIMPLE_TYPE(
            "simpleType",
            ( schema, item, event ) -> item.setAttributes( event.asStringMap() ),
            SchemaLeafTag.RESTRICTION,
            SchemaLeafTag.ANNOTATION,
            SchemaLeafTag.LIST )
            {
                @Override
                public SimpleTypeObject getItem( SchemaObject schemaObject, OpenEvent event )
                {
                    SimpleTypeObject item = new SimpleTypeObject( null );
                    schemaObject.getSimpleTypes().add( item );
                    return item;
                }
            };


    private final String tag;
    private final boolean choice = true;
    private final StepOpener< SchemaObject, SchemaItem, OpenEvent > opener;
    private final boolean multiple = true;
    private final Tag< ? super SchemaItem, ? >[] children;

    @SafeVarargs
    SchemaReferenceTag(
            String tag,
            TriConsumer< SchemaObject, SchemaItem, OpenEvent > opener,
            Tag< ? super SchemaItem, ? >... children
    )
    {
        this.tag = tag;
        this.opener = Opener.stepOpener( opener );
        this.children = children;
    }
}


@Getter
enum SchemaElementTag implements StepTag< SchemaItem, SchemaItem >
{
    ELEMENT(
            "element",
            ( item, event ) -> item.setAttributes( event.asStringMap() ),
            SchemaLeafTag.COMPLEX_TYPE,
            SchemaLeafTag.SIMPLE_TYPE,
            SchemaLeafTag.ANNOTATION )
            {
                @Override
                public ElementObject getItem( SchemaItem schemaItem, OpenEvent event )
                {
                    ElementObject item = new ElementObject( schemaItem );
                    schemaItem.addElement( item );
                    return item;
                }
            };


    private final String tag;
    private final boolean choice = true;
    private final FlatOpener< SchemaItem, OpenEvent > opener;
    private final boolean multiple = true;
    private final Tag< ? super SchemaItem, ? >[] children;

    @SafeVarargs
    SchemaElementTag(
            String tag,
            BiConsumer< SchemaItem, OpenEvent > opener,
            Tag< ? super SchemaItem, ? >... children
    )
    {
        this.tag = tag;
        this.opener = Opener.flatOpener( opener );
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
    private final boolean multiple = true;
    private final boolean choice = true;
    private final FlatCacheOpener< SchemaItem, OpenEvent, ? > opener;
    private final FlatCacheCloser< SchemaItem, String, ? > closer;
    private final Tag< ? super SchemaItem, ? >[] children;


    @SafeVarargs
    SchemaLeafTag( String tag, Tag< ? super SchemaItem, ? >... children )
    {
        this( tag, null, null, children );
    }

    @SafeVarargs
    < C > SchemaLeafTag(
            String tag,
            BiFunction< SchemaItem, OpenEvent, C > opener,
            TriConsumer< SchemaItem, String, C > closer,
            Tag< ? super SchemaItem, ? >... children
    )
    {
        this.tag = tag;
        this.opener = Opener.flatCacheOpener( opener );
        this.closer = Closer.flatCacheCloser( closer );
        this.children = children;
    }
}
