package com.brentcroft.tools.materializer;


import com.brentcroft.tools.materializer.core.Tag;
import com.brentcroft.tools.materializer.core.TagHandler;
import lombok.Getter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.nonNull;

@Getter
public class Materializer< R > implements Function< InputSource, R >
{
    private final Schema schema;
    private final SAXParserFactory saxParserFactory;
    private final List< SAXParser > parsers = new LinkedList<>();

    private final Supplier< Tag.FlatTag< ? > > rootTagSupplier;
    private final Supplier< R > rootItemSupplier;

    public Materializer( Schema schema, int initialPoolSize, Supplier< Tag.FlatTag< ? > > rootTagSupplier, Supplier< R > rootItemSupplier )
    {
        this.schema = schema;
        this.saxParserFactory = SAXParserFactory.newInstance();
        this.rootTagSupplier = rootTagSupplier;
        this.rootItemSupplier = rootItemSupplier;

        saxParserFactory.setNamespaceAware( true );

        if ( nonNull( schema ) )
        {
            saxParserFactory.setSchema( schema );
        }

        try
        {
            for ( int i = 0; i < initialPoolSize; i++ )
            {
                releaseParser( saxParserFactory.newSAXParser() );
            }
        }
        catch ( SAXException | ParserConfigurationException e )
        {
            throw new IllegalArgumentException( e );
        }
    }

    private void releaseParser( SAXParser parser )
    {
        if ( nonNull( parser ) )
        {
            synchronized ( parsers )
            {
                parsers.add( parser );
            }
        }
    }


    @Override
    public R apply( InputSource inputSource )
    {
        R rootItem = rootItemSupplier.get();

        TagHandler tagHandler = new TagHandler( rootTagSupplier.get(), rootItem );

        SAXParser parser = null;

        try
        {
            parser = getParser();

            parser.parse( inputSource, tagHandler );
        }
        catch ( ParserConfigurationException e )
        {
            throw new TagParseException( tagHandler, e );
        }
        catch ( SAXException e )
        {
            throw new TagParseException( tagHandler, e );
        }
        catch ( IOException e )
        {
            throw new TagParseException( tagHandler, e );
        }
        catch ( TagException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            throw new TagException( tagHandler, e );
        }
        finally
        {
            releaseParser( parser );
        }

        return rootItem;
    }

    private SAXParser getParser() throws ParserConfigurationException, SAXException
    {
        synchronized ( parsers )
        {
            if ( parsers.isEmpty() )
            {
                return saxParserFactory.newSAXParser();
            }
            SAXParser parser = parsers.remove( 0 );
            parser.reset();
            return parser;
        }
    }


    public static Schema getSchemas( String... uris )
    {
        List< String > schemaUris = Arrays.asList( uris );

        Source[] sources = schemaUris
                .stream()
                .map( uri -> new StreamSource(
                        Thread
                                .currentThread()
                                .getContextClassLoader()
                                .getResourceAsStream( uri ), uri ) )
                .collect( Collectors.toList() )
                .toArray( new Source[ uris.length ] );

        try
        {

            return SchemaFactory
                    .newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI )
                    .newSchema( sources );
        }
        catch ( SAXException e )
        {
            throw new IllegalArgumentException(
                    format( "Failed to load schema uris [%s]: %s", schemaUris, e.getMessage() ), e );
        }
    }

}
