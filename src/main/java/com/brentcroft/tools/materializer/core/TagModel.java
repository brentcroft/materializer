package com.brentcroft.tools.materializer.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.xml.sax.Attributes;

import static java.lang.String.format;

@Getter
@RequiredArgsConstructor
public class TagModel< R >
{
    private final Tag< ?, R > parent;
    private final boolean choice;
    private final Tag< ? super R, ? >[] children;

    private int index = - 1;

    public Tag< ? super R, ? > getTag( String uri, String localName, String qName, Attributes attributes )
    {
        if ( choice )
        {
            for ( Tag< ? super R, ? > tag : children )
            {
                if ( tag.matches( uri, localName, qName, attributes ) )
                {
                    return tag;
                }
            }

            throw new ValidationException( parent, format( "Unexpected tag: no choice matches localName <%s>", localName ) );
        }
        else if ( - 1 < index && index < children.length )
        {
            Tag< ? super R, ? > tag = children[ index ];

            if ( tag.matches( uri, localName, qName, attributes ) && tag.isMultiple() )
            {
                return tag;
            }
        }

        // sequence advance
        index++;

        while ( index < children.length )
        {
            final Tag< ? super R, ? > tag = children[ index ];

            if ( tag.matches( uri, localName, qName, attributes ) )
            {
                return tag;
            }
            else if ( ! tag.isOptional() )
            {
                throw new ValidationException(
                        parent,
                        format( "Unexpected tag: mandatory tag <%s> does not match localName <%s>", tag.getTag(), localName ) );
            }

            index++;
        }

        throw new ValidationException( parent, format( "Unexpected tag: no child matches localName <%s>", localName ) );

    }
}
