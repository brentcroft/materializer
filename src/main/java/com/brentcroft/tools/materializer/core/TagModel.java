package com.brentcroft.tools.materializer.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.xml.sax.Attributes;

@Getter
@RequiredArgsConstructor
public class TagModel
{
    private final boolean choice;
    private final Tag< ?, ? >[] children;

    private int index = - 1;

    public Tag< ?, ? > getTag( String uri, String localName, String qName, Attributes attributes )
    {
        if ( index > - 1 )
        {
            Tag< ?, ? > tag = children[ index ];

            if ( ( tag.getTag().equals( "*" ) || tag.getTag().equals( localName ) ) && tag.isMultiple() )
            {
                return tag;
            }
        }

        if ( choice )
        {
            for ( Tag< ?, ? > tag : children )
            {
                if ( ( tag.getTag().equals( "*" ) || tag.getTag().equals( localName ) ) )
                {
                    return tag;
                }
            }
        }

        // increment
        index++;

        if ( index >= children.length )
        {
            throw new IllegalArgumentException( "Unexpected child: no more children: " + localName );
        }

        Tag< ?, ? > tag = children[ index ];

        if ( ( tag.getTag().equals( "*" ) || tag.getTag().equals( localName ) ) )
        {
            return tag;
        }

        throw new IllegalArgumentException( "Unexpected child: unrecognised tag: " + localName );
    }
}
