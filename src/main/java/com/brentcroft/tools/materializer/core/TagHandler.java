package com.brentcroft.tools.materializer.core;

import com.brentcroft.tools.materializer.ContextValue;
import com.brentcroft.tools.materializer.TagHandlerException;
import com.brentcroft.tools.materializer.ValidationException;
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
public class TagHandler< R > extends DefaultHandler implements TagHandlerContext
{
    @Setter
    private Locator documentLocator;

    private final Stack< Object > itemStack = new Stack<>();
    private final Stack< TagContext > contextStack;
    private final ContextValue contextValue;


    private final StringBuilder characters = new StringBuilder();

    public TagHandler( R rootItem, TagContext rootContext, ContextValue contextValue )
    {
        contextStack = new Stack<>();
        contextStack.push( rootContext );
        itemStack.push( rootItem );
        this.contextValue = contextValue;
    }
    public TagHandler( R rootItem,  Stack< TagContext > tagContextStack, ContextValue contextValue )
    {
        contextStack = tagContextStack;
        itemStack.push( rootItem );
        this.contextValue = contextValue;
    }


    @Override
    public String getPath()
    {
        return contextStack
                .stream()
                .filter( t -> ! t.getTag().isJump() )
                .map( TagContext::getTag )
                .map( Tag::getTag )
                .collect( Collectors.joining( "/" ) );
    }


    public void startElement( String uri, String localName, String qName, Attributes attributes )
    {
        final OpenEvent openEvent = new OpenEvent(
                uri,
                localName,
                qName,
                attributes,
                this,
                contextStack
                        .peek()
                        .getEvent()
                        .inContext() );

        characters.setLength( 0 );

        if ( contextStack.isEmpty() )
        {
            throw new TagHandlerException( this, format( "Empty stack for localName: '%s'", openEvent.combinedTag() ) );
        }

        TagContext tagContext = contextStack.peek();

        if ( isNull( tagContext.getModel() ) )
        {
            throw new TagHandlerException( this, format( "Unexpected tag '%s': %s does not accept children.", openEvent.combinedTag(), contextStack.peek() ) );
        }

        Tag< ?, ? > tag;


        Object contextItem = itemStack.peek();

        try
        {
            tag = tagContext.getModel().getTag( openEvent );

            while ( tag instanceof JumpTag )
            {
                Object jumpItem = ( ( JumpTag< ?, ? > ) tag ).step( contextItem, openEvent );

                tagContext = new TagContext(
                        openEvent,
                        tag.open( contextItem, jumpItem, openEvent ),
                        tag,
                        tag.getTagModel() );

                contextStack.push( tagContext );
                itemStack.push( jumpItem );

                contextItem = jumpItem;
                tag = tag.getTagModel().getTag( openEvent );
            }
        }
        catch ( ValidationException e )
        {
            throw new TagHandlerException( this, e );
        }

        openEvent.setTag( tag );

        Object item;

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

        contextStack
                .push( new TagContext(
                        openEvent,
                        tag.open( contextItem, item, openEvent ),
                        tag,
                        tag.getTagModel() ) );
    }

    public void endElement( String uri, String localName, String qName )
    {
        TagContext tagContext = contextStack.peek();

        OpenEvent event = tagContext.getEvent();
        Object cachedObject = tagContext.getCache();
        Tag< ?, ? > tag = tagContext.getTag();

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
        contextStack.pop();
        characters.setLength( 0 );

        Tag< ?, ? > jumpTag = contextStack.isEmpty() ? null : contextStack.peek().getTag();

        // unwind any jumps
        while ( jumpTag instanceof JumpTag )
        {
            tagContext = contextStack.pop();

            Object item = itemStack.pop();
            Object contextItem = itemStack.peek();

            // jump can't collect text
            jumpTag.close( contextItem, item, null, tagContext.getCache() );

            jumpTag = contextStack.isEmpty() ? null : contextStack.peek().getTag();
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
