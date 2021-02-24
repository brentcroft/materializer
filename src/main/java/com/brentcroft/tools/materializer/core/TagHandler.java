package com.brentcroft.tools.materializer.core;

import com.brentcroft.tools.materializer.ContextValue;
import com.brentcroft.tools.materializer.TagHandlerException;
import com.brentcroft.tools.materializer.ValidationException;
import com.brentcroft.tools.materializer.model.FlatTag;
import com.brentcroft.tools.materializer.model.JumpTag;
import com.brentcroft.tools.materializer.model.StepTag;
import lombok.Getter;
import lombok.Setter;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Stack;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

@Getter
public class TagHandler< R > extends DefaultHandler
{
    @Setter
    private Locator documentLocator;

    @Setter
    private ContextValue contextValue;

    private final Stack< OpenEvent > eventStack = new Stack<>();
    private final Stack< Object > itemStack = new Stack<>();
    private final Stack< Object > cacheStack = new Stack<>();

    private final Stack< Tag< ?, ? > > tagStack = new Stack<>();
    private final Stack< TagModel< ? > > tagModelStack = new Stack<>();

    private final StringBuilder characters = new StringBuilder();

    public TagHandler( FlatTag< ? super R > rootTag, R rootItem )
    {
        tagStack.push( rootTag );
        itemStack.push( rootItem );
        tagModelStack.push( rootTag.getTagModel() );
    }

    public String getPath()
    {
        return tagStack
                .stream()
                .filter( t -> ! ( t instanceof JumpTag ) )
                .map( Tag::getTag )
                .collect( Collectors.joining( "/" ) );
    }

    public void startDocument()
    {
        eventStack.push( new OpenEvent( null, null, null, null, contextValue ) );
    }

    public void startElement( String uri, String localName, String qName, Attributes attributes )
    {
        OpenEvent openEvent = new OpenEvent(
                uri,
                localName,
                qName,
                attributes,
                eventStack
                        .peek()
                        .inContext() );

        characters.setLength( 0 );

        if ( tagModelStack.isEmpty() )
        {
            throw new TagHandlerException( this, format( "Empty stack for localName: '%s'", openEvent.combinedTag() ) );
        }
        else if ( isNull( tagModelStack.peek() ) )
        {
            throw new TagHandlerException( this, format( "Unexpected tag '%s': %s does not accept children.", openEvent.combinedTag(), tagStack.peek() ) );
        }

        Tag< ?, ? > tag;

        Object contextItem = itemStack.peek();

        try
        {
            tag = tagModelStack.peek().getTag( openEvent );

            while ( tag instanceof JumpTag )
            {
                Object jumpItem = ( ( JumpTag< ?, ? > ) tag ).step( contextItem, openEvent );

                itemStack.push( jumpItem );
                tagStack.push( tag );
                tagModelStack.push( tag.getTagModel() );
                cacheStack.push( tag.open( contextItem, jumpItem, openEvent ) );

                Tag< ?, ? > jumpTag = tag.getTagModel().getTag( openEvent );

                //System.out.printf( "jump: %s -> %s%n", tag, jumpTag );

                contextItem = jumpItem;
                tag = jumpTag;
            }
        }
        catch ( ValidationException e )
        {
            throw new TagHandlerException( this, e );
        }

        openEvent.setTag( tag );

        Object item;

        //System.out.printf( "key=%s, tag=%s, item=%s %n", localName, tag, item.getClass().getSimpleName() );

        if ( tag instanceof StepTag )
        {
            // risk of ClassCastException
            item = ( ( StepTag< ?, ? > ) tag ).step( contextItem, openEvent );

            itemStack.push( item );
        }
        else
        {
            item = contextItem;
        }

        if ( isNull( item ) )
        {
            throw new TagHandlerException( this, format( "No item obtained for tag: %s", tag.getTag() ) );
        }

        Object cachedObject = tag.open( contextItem, item, openEvent );

        tagModelStack.push( tag.getTagModel() );
        tagStack.push( tag );
        cacheStack.push( cachedObject );
        eventStack.push( openEvent );
    }

    public void endElement( String uri, String localName, String qName )
    {
        OpenEvent event = eventStack.pop();

        Object cachedObject = cacheStack.peek();
        Tag< ?, ? > tag = tagStack.peek();

        if ( nonNull( tag ) )
        {
            Object item;

            if ( tag instanceof StepTag )
            {
                item = itemStack.pop();
            }
            else
            {
                item = itemStack.peek();
            }

            Object contextItem = itemStack.peek();

            String text = characters.toString();

            // stacks identify state if exception thrown
            tag.close(
                    contextItem,
                    item,
                    ofNullable( event.getContextValue() )
                            .map( cv -> cv.map( "text", text ) )
                            .orElse( text ),
                    cachedObject );
        }

        // pop the stacks
        cacheStack.pop();
        tagModelStack.pop();
        tagStack.pop();
        characters.setLength( 0 );

        Tag< ?, ? > jumpTag = tagStack.isEmpty() ? null : tagStack.peek();

        // unwind any jumps
        while ( jumpTag instanceof JumpTag )
        {
            cachedObject = cacheStack.pop();
            tagModelStack.pop();
            tagStack.pop();

            Object item = itemStack.pop();
            Object contextItem = itemStack.peek();

            // jump can't collect text
            jumpTag.close( contextItem, item, null, cachedObject );

            jumpTag = tagStack.isEmpty() ? null : tagStack.peek();

            //System.out.printf( "return: %s <- %s%n", tag, jumpTag );
        }
    }

    public void characters( char[] ch, int start, int length )
    {
        characters.append( ch, start, length );
    }

    public void error( SAXParseException spe ) throws SAXException
    {
        throw spe;
    }

    public void fatalError( SAXParseException spe ) throws SAXException
    {
        throw spe;
    }
}
