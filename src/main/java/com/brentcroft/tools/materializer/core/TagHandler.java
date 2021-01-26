package com.brentcroft.tools.materializer.core;

import com.brentcroft.tools.materializer.TagHandlerException;
import lombok.Getter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Iterator;
import java.util.Stack;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Getter
public class TagHandler extends DefaultHandler
{
    private final Stack< Object > rootItemStack = new Stack<>();

    private final Stack< Tag< ?, ? > > tagStack = new Stack<>();
    private final Stack< Iterator< Tag< ?, ? > > > tagStackIterator = new Stack<>();
    private final StringBuilder characters = new StringBuilder();
    private Tag< ?, ? > lastTag;


    public TagHandler( Tag< ?, ? > rootTag, Object rootItem )
    {
        rootItemStack.push( rootItem );
        tagStack.push( rootTag );
        tagStackIterator.push( rootTag.getIterator() );
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

        Tag< ?, ? > tag = ( nonNull( lastTag ) && lastTag.getTag().equals( localName ) && lastTag.isMultiple() )
                          ? lastTag
                          : null;

        lastTag = null;

        if ( isNull( tag ) )
        {
            tag = tagStackIterator.peek().hasNext()
                  ? tagStackIterator.peek().next()
                  : null;

            if ( isNull( tag ) )
            {
                throw new TagHandlerException( this, format( "No element expected: <%s>; %s", localName, tag ) );
            }
            else
            {
                while ( ! tag.getTag().equals( localName ) && tag.isOptional() )
                {
                    tag = tagStackIterator.peek().next();
                }
            }

            if ( ! tag.getTag().equals( localName ) )
            {
                throw new TagHandlerException( this, format( "Unexpected element: <%s>; expected: <%s>", localName, tag.getTag() ) );
            }
        }


        Object item = rootItemStack.peek();

        //System.out.printf( "tag=%s, item=%s %n", tag, item.getClass().getSimpleName() );

        if ( tag instanceof StepTag )
        {
            // risk of ClassCastException
            item = ((StepTag<?,?>)tag).step( item );

            rootItemStack.push( item );
        }

        if ( isNull( item ) )
        {
            throw new TagHandlerException( this, format( "No item obtained for tag: <%s>", tag.getTag() ) );
        }

        tagStack.push( tag );
        tagStackIterator.push( tag.getIterator() );

        tag.open( item, attributes );
    }

    public void endElement( String uri, String localName, String qName )
    {
        Tag< ?, ? > tag =  tagStack.peek();

        if ( nonNull( tag ) )
        {
            Object item = rootItemStack.peek();

            // stacks identify state if exception thrown
            tag.close( item, characters.toString().trim() );

            if ( tag instanceof StepTag )
            {
                rootItemStack.pop();
            }
        }

        lastTag = tag;

        // pop the stacks
        tagStackIterator.pop();
        tagStack.pop();
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
