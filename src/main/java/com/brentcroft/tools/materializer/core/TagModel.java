package com.brentcroft.tools.materializer.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.xml.sax.Attributes;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.isNull;

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

            throw new ValidationException(
                    parent,
                    format(
                            "Unexpected tag '%s': no choice of %s['%s'] matches any child: %s.",
                            combinedTag( localName, qName ),
                            parent.name(),
                            parent.getTag(),
                            Stream
                                    .of( children )
                                    .map( c -> format( "%s['%s']", c.name(), c.getTag() ) )
                                    .collect( Collectors.joining( ", " ) )
                    ) );
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
                        format(
                                "Unexpected tag '%s': mandatory tag %s['%s'] does not match.",
                                combinedTag( localName, qName ),
                                tag.name(),
                                tag.getTag()
                        ) );
            }

            index++;
        }

        throw new ValidationException(
                parent,
                format(
                        "Unexpected tag '%s': no child matches: %s.",
                        combinedTag( localName, qName ),
                        Stream
                                .of( children )
                                .map( c -> format( "%s['%s']", c.name(), c.getTag() ) )
                                .collect( Collectors.joining( ", " ) )));

    }

    private String combinedTag( String localName, String qName )
    {
        return isNull( localName ) || localName.isEmpty()
               ? qName
               : isNull( qName ) || qName.isEmpty() || localName.equals( qName )
                 ? localName
                 : format( "%s | %s", localName, qName );
    }
}
