package com.brentcroft.tools.materializer.util;

import com.brentcroft.tools.materializer.core.FlatTag;
import com.brentcroft.tools.materializer.core.Tag;
import com.brentcroft.tools.materializer.core.TriConsumer;
import lombok.Getter;
import org.xml.sax.Attributes;

import java.util.Properties;
import java.util.function.BiFunction;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

@Getter
public enum PropertiesRootTag implements FlatTag< Properties >
{
    COMMENT( "comment" ),
    ENTRY( "entry",

            // open: cache attributes.key
            ( properties, attributes ) -> ofNullable( attributes.getValue( "key" ) )
                    .filter( v -> ! v.isEmpty() )
                    .orElseThrow( () -> new RuntimeException( "entry element has no attribute key!" ) ),

            // close: de-cache attributes.key
            ( properties, text, cache ) -> properties.setProperty( cache.toString(), text ) ),

    PROPERTIES( "*", ENTRY, COMMENT ),

    ROOT( "", PROPERTIES );


    private final String tag;
    private final FlatTag< Properties > self = this;
    private final boolean multiple;
    private final boolean choice;
    private final BiFunction< Properties, Attributes, ? > opener;
    private final TriConsumer< Properties, String, Object > closer;
    private final Tag< ? super Properties, ? >[] children;

    @SafeVarargs
    PropertiesRootTag( String tag, Tag< ? super Properties, ? >... children )
    {
        this( tag, null, null, children );
    }

    @SafeVarargs
    PropertiesRootTag( String tag, BiFunction< Properties, Attributes, ? > opener, TriConsumer< Properties, String, Object > closer, Tag< ? super Properties, ? >... children )
    {
        this.tag = tag;
        this.multiple = isNull( children ) || children.length == 0;
        this.opener = opener;
        this.closer = closer;
        this.choice = nonNull( children ) && children.length > 0;
        this.children = children;
    }
}
