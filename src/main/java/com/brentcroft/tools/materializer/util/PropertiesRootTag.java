package com.brentcroft.tools.materializer.util;

import com.brentcroft.tools.materializer.core.FlatTag;
import com.brentcroft.tools.materializer.core.Tag;
import lombok.Getter;
import org.xml.sax.Attributes;

import java.util.Properties;
import java.util.function.BiConsumer;

import static java.util.Objects.nonNull;

@Getter
public enum PropertiesRootTag implements FlatTag< Properties >
{
    COMMENT( "comment", true ),
    ENTRY( "entry", true,

            // open: cache attributes.key
            ( properties, attributes ) -> properties
                    .setProperty(
                            "$currentKey",
                            attributes.getValue( "key" ) ),

            // close: de-cache attributes.key
            ( properties, text ) -> properties
                    .setProperty(
                            properties.getProperty( "$currentKey" ),
                            text ) ),

    PROPERTIES( "*", false,

            // open: create cache
            ( properties, attributes ) -> properties.setProperty( "$currentKey", "" ),

            // close:  remove cache
            ( properties, text ) -> properties.remove( "$currentKey" ),

            ENTRY, COMMENT ),

    ROOT( "", false, PROPERTIES );


    private final String tag;
    private final FlatTag< Properties > self = this;
    private final boolean multiple;
    private final boolean choice;
    private final BiConsumer< Properties, Attributes > opener;
    private final BiConsumer< Properties, String > closer;
    private final Tag< ? super Properties, ? >[] children;

    @SafeVarargs
    PropertiesRootTag( String tag, boolean multiple, Tag< ? super Properties, ? >... children )
    {
        this( tag, multiple, null, null, children );
    }

    @SafeVarargs
    PropertiesRootTag( String tag, boolean multiple, BiConsumer< Properties, Attributes > opener, BiConsumer< Properties, String > closer, Tag< ? super Properties, ? >... children )
    {
        this.tag = tag;
        this.multiple = multiple;
        this.opener = opener;
        this.closer = closer;
        this.choice = nonNull( children ) && children.length > 0;
        this.children = children;
    }
}
