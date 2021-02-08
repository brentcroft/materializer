package com.brentcroft.tools.materializer.util;

import com.brentcroft.tools.materializer.core.Closer;
import com.brentcroft.tools.materializer.core.FlatTag;
import com.brentcroft.tools.materializer.core.Opener;
import com.brentcroft.tools.materializer.core.Tag;
import lombok.Getter;
import org.xml.sax.Attributes;

import java.util.Optional;
import java.util.Properties;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Getter
public enum PropertiesRootTag implements FlatTag< Properties >
{
    ENTRY( "entry", String.class,

            // open: cache attribute @key
            ( properties, attributes ) -> Optional
                    .ofNullable( Tag.getAttributesMap( attributes ).get( "key" ) )
                    .map( Object::toString )
                    .orElseThrow( () -> new IllegalArgumentException( "missing attribute: key" ) ),

            // close: de-cache
            ( properties, text, cache ) -> properties.setProperty( cache, text ) ),

    COMMENT( "comment" ),
    PROPERTIES( "*", ENTRY, COMMENT ),
    ROOT( "", PROPERTIES );

    private final String tag;
    private final FlatTag< Properties > self = this;
    private final boolean multiple;
    private final boolean choice;
    private final Opener< Properties, Attributes, ? > opener;
    private final Closer< Properties, String, ? > closer;
    private final Tag< ? super Properties, ? >[] children;

    @SafeVarargs
    PropertiesRootTag( String tag, Tag< ? super Properties, ? >... children )
    {
        this( tag, Object.class, null, null, children );
    }

    @SafeVarargs
    < C > PropertiesRootTag(
            String tag,
            Class< C > c,
            Opener< Properties, Attributes, C > opener,
            Closer< Properties, String, C > closer,
            Tag< ? super Properties, ? >... children
    )
    {
        this.tag = tag;
        this.multiple = isNull( children ) || children.length == 0;
        this.opener = opener;
        this.closer = closer;
        this.choice = nonNull( children ) && children.length > 0;
        this.children = children;
    }
}
