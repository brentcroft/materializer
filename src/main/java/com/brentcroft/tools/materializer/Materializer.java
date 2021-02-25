package com.brentcroft.tools.materializer;


import com.brentcroft.tools.materializer.core.OpenEvent;
import com.brentcroft.tools.materializer.core.TagContext;
import com.brentcroft.tools.materializer.core.TagHandler;
import com.brentcroft.tools.materializer.model.FlatTag;
import lombok.Getter;
import lombok.Setter;
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
import java.util.Stack;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.nonNull;

/**
 * A Materializer provides a function to build an object
 * during the SAX parsing of an XML InputSource using a specialized TagHandler.
 *
 * @param <R> the type of object to materialize.
 */
@Getter
public class Materializer< R > implements Function< InputSource, R >
{

    private final Schema schema;
    private final SAXParserFactory saxParserFactory;
    private final List< SAXParser > parsers = new LinkedList<>();
    private final Supplier< FlatTag< ? super R > > rootTagSupplier;
    private final Supplier< R > rootItemSupplier;

    private Stack< TagContext > contextStack;

    @Setter
    private ContextValue contextValue;

    public Materializer( Supplier< FlatTag< ? super R > > rootTagSupplier, Supplier< R > rootItemSupplier )
    {
        this( rootTagSupplier, rootItemSupplier, null );
    }

    public Materializer( Supplier< FlatTag< ? super R > > rootTagSupplier, Supplier< R > rootItemSupplier, Stack< TagContext > contextStack )
    {
        this( null, 0, rootTagSupplier, rootItemSupplier, contextStack );
    }

    public Materializer( int initialPoolSize, Supplier< FlatTag< ? super R > > rootTagSupplier, Supplier< R > rootItemSupplier )
    {
        this( null, initialPoolSize, rootTagSupplier, rootItemSupplier, null );
    }

    public Materializer( Schema schema, int initialPoolSize, Supplier< FlatTag< ? super R > > rootTagSupplier, Supplier< R > rootItemSupplier )
    {
        this( schema, initialPoolSize, rootTagSupplier, rootItemSupplier, null );
    }

    public static < R > Stack< TagContext > newStackInContext( FlatTag< ? super R > rootTag, ContextValue contextValue )
    {
        OpenEvent event = new OpenEvent(
                null,
                null,
                null,
                null,
                null,
                contextValue );

        event.setTag( rootTag );

        Stack< TagContext > stack = new Stack<>();
        stack
                .push(
                        new TagContext(
                                event,
                                null,
                                rootTag,
                                rootTag.getTagModel() ) );
        return stack;
    }

    public Materializer( Schema schema, int initialPoolSize, Supplier< FlatTag< ? super R > > rootTagSupplier, Supplier< R > rootItemSupplier, Stack< TagContext > contextStack )
    {
        this.schema = schema;
        this.saxParserFactory = SAXParserFactory.newInstance();
        this.rootTagSupplier = rootTagSupplier;
        this.rootItemSupplier = rootItemSupplier;
        this.contextStack = contextStack;

        saxParserFactory.setNamespaceAware( true );

        if ( nonNull( schema ) )
        {
            saxParserFactory.setSchema( schema );
        }

        try
        {
            saxParserFactory.setFeature( "http://xml.org/sax/features/namespace-prefixes", true );

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

    /**
     * Caller's responsibility to capture the rootItem.
     *
     * @return a TagHandler on a root tag and root item
     */
    public TagHandler< R > getDefaultHandler()
    {
        R rootItem = rootItemSupplier.get();

        return new TagHandler<>( rootItem, contextStack, contextValue );
    }


    @Override
    public R apply( InputSource inputSource )
    {
        R rootItem = rootItemSupplier.get();

        Stack< TagContext > stack = nonNull( contextStack )
                                    ? contextStack
                                    : newStackInContext( rootTagSupplier.get(), contextValue );

        TagHandler< R > tagHandler = new TagHandler<>( rootItem, stack, contextValue );

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
        catch ( ValidationException e )
        {
            throw new TagValidationException( tagHandler, e );
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

            stack.pop();
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
}
