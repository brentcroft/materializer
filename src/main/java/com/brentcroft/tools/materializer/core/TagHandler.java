package com.brentcroft.tools.materializer.core;

import com.brentcroft.tools.materializer.TagHandlerException;
import lombok.Getter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Stack;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Getter
public class TagHandler< R > extends DefaultHandler
{
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
                .map( Tag::getTag )
                .collect( Collectors.joining( "/" ) );
    }

    public void startElement( String uri, String localName, String qName, Attributes attributes )
    {
        characters.setLength( 0 );

        if ( tagModelStack.isEmpty() )
        {
            throw new TagHandlerException( this, format( "Empty stack for localName: '%s'", localName ) );
        }
        else if ( isNull( tagModelStack.peek() ) )
        {
            throw new TagHandlerException( this, format( "Model on stack is null for localName: '%s'", localName ) );
        }


        Tag< ?, ? > tag = tagModelStack.peek().getTag( uri, localName, qName, attributes );


        Object item = itemStack.peek();

        //System.out.printf( "key=%s, tag=%s, item=%s %n", localName, tag, item.getClass().getSimpleName() );

        if ( tag instanceof StepTag )
        {
            // risk of ClassCastException
            item = ( ( StepTag< ?, ? > ) tag ).step( item );

            itemStack.push( item );
        }

        if ( isNull( item ) )
        {
            throw new TagHandlerException( this, format( "No item obtained for tag: %s", tag.getTag() ) );
        }

        tagStack.push( tag );
        tagModelStack.push( tag.getTagModel() );
        cacheStack.push( tag.open( item, attributes ) );
    }

    public void endElement( String uri, String localName, String qName )
    {
        Object cachedObject = cacheStack.pop();
        Tag< ?, ? > tag = tagStack.pop();

        if ( nonNull( tag ) )
        {
            Object item = itemStack.peek();

            // stacks identify state if exception thrown
            tag.close( item, characters.toString().trim(), cachedObject );

            if ( tag instanceof StepTag )
            {
                itemStack.pop();
            }
        }

        // pop the stacks
        tagModelStack.pop();
        characters.setLength( 0 );
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
