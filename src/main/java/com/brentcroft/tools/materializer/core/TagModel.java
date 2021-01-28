package com.brentcroft.tools.materializer.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.xml.sax.Attributes;

@Getter
@RequiredArgsConstructor
public class TagModel< R >
{
    private final boolean choice;
    private final Tag< ? super R, ? >[] children;

    private int index = - 1;

    public Tag< ? super R, ? > getTag( String uri, String localName, String qName, Attributes attributes )
    {
        if ( index > - 1 )
        {
            Tag< ? super R, ? > tag = children[ index ];

            if ( ( tag.getTag().equals( "*" ) || tag.getTag().equals( localName ) ) && tag.isMultiple() )
            {
                return tag;
            }
        }

        if ( choice )
        {
            for ( Tag< ? super R, ? > tag : children )
            {
                if ( ( tag.getTag().equals( "*" ) || tag.getTag().equals( localName ) ) )
                {
                    return tag;
                }
            }
        }

        index++;

        while ( index < children.length )
        {
            final Tag< ? super R, ? > tag = children[ index ];

            if ( ( tag.getTag().equals( "*" ) || tag.getTag().equals( localName ) ) )
            {
                return tag;
            }
            else if ( ! tag.isOptional() )
            {
                throw new IllegalArgumentException( "Unexpected tag: unrecognised: " + localName );
            }

            index++;
        }

        throw new IllegalArgumentException( "Unexpected tag: no more children: " + localName );
    }
}
