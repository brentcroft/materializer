package com.brentcroft.tools.materializer.core;

import com.brentcroft.tools.materializer.ValidationException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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

    public Tag< ? super R, ? > getTag( OpenEvent openEvent )
    {
        if ( isNull( children ) || children.length == 0 )
        {
            throw new ValidationException(
                    parent,
                    format(
                            "Unexpected tag '%s': no children expected.",
                            openEvent.combinedTag()
                    ) );
        }

        if ( choice )
        {
            for ( Tag< ? super R, ? > tag : children )
            {
                if ( isNull( tag ) )
                {
                    throw new ValidationException(
                            parent,
                            format(
                                    "TagModel has empty child item: index=%s, parent=%s %s",
                                    index,
                                    parent.name(),
                                    parent.getTag()
                            ) );
                }

                if ( tag.matches( openEvent ) )
                {
                    return tag;
                }
            }

            throw new ValidationException(
                    parent,
                    format(
                            "Unexpected tag '%s': no choice of %s['%s'] matches any child: %s.",
                            openEvent.combinedTag(),
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
            final Tag< ? super R, ? > tag = children[ index ];

            if ( tag.isMultiple() && tag.matches( openEvent ) )
            {
                return tag;
            }
            // fall through
        }

        // sequence advance
        index++;

        while ( index < children.length )
        {
            final Tag< ? super R, ? > tag = children[ index ];

            if ( isNull( tag ) )
            {
                throw new ValidationException(
                        parent,
                        format(
                                "TagModel has empty child item: index=%s, parent=%s %s",
                                index,
                                parent.name(),
                                parent.getTag()
                        ) );
            }

            if ( tag.matches( openEvent ) )
            {
                return tag;
            }
            else if ( ! tag.isOptional() )
            {
                throw new ValidationException(
                        parent,
                        format(
                                "Unexpected tag '%s': mandatory tag %s['%s'] does not match.",
                                openEvent.combinedTag(),
                                tag.name(),
                                tag.getTag()
                        ) );
            }

            index++;
        }

        throw new ValidationException(
                parent,
                format(
                        "Unexpected tag '%s': no child of '%s' matches: %s.",
                        openEvent.combinedTag(),
                        parent.getTag(),
                        Stream
                                .of( children )
                                .map( c -> format( "%s['%s']", c.name(), c.getTag() ) )
                                .collect( Collectors.joining( ", " ) ) ) );
    }
}
