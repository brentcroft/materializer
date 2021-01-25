package com.brentcroft.tools.materializer.core;

import com.brentcroft.tools.materializer.TagHandlerException;
import lombok.Getter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Iterator;
import java.util.Stack;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

@Getter
public class TagHandler extends DefaultHandler
{
    private final Stack< Object > rootItemStack = new Stack<>();

    private final Stack< Tag< ?, ? > > tagStack = new Stack<>();
    private final Stack< Iterator< Tag< ?, ? > > > tagStackIterator = new Stack<>();
    private final StringBuilder characters = new StringBuilder();
    private Tag< ?, ? > lastSibling;


    public TagHandler( Tag< ?, ? > rootTag, Object rootItem )
    {
        rootItemStack.push( rootItem );
        tagStack.push( rootTag );
        tagStackIterator.push( rootTag.getIterator() );
    }

    public void startDocument()
    {
    }

    public void startElement( String uri, String localName, String qName, Attributes attributes )
    {
        characters.setLength( 0 );

        Tag< ?, ? > tag = ( nonNull( lastSibling ) && lastSibling.getTag().equals( localName ) && lastSibling.isMultiple() )
                          ? lastSibling
                          : null;

        lastSibling = null;

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

        Object rootItem = tag.upcastItem( rootItemStack.peek() ) ;

        if (isNull(rootItem))
        {
            throw new TagHandlerException( this, format( "No item obtained for tag: <%s>", tag.getTag() ) );
        }

        rootItemStack.push( rootItem );

        tagStack.push( tag );
        tagStackIterator.push( tag.getIterator() );

        tag.open( rootItem, attributes );
    }

    public void endElement( String uri, String localName, String qName )
    {
        lastSibling = tagStack.pop();
        tagStackIterator.pop();
        Object rootItem = rootItemStack.pop();

        if ( nonNull( lastSibling ) )
        {
            lastSibling.close( rootItem, characters.toString().trim() );
        }

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
