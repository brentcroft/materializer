package com.brentcroft.tools.materializer.util;

import com.brentcroft.tools.materializer.core.FlatTag;
import com.brentcroft.tools.materializer.core.Tag;
import lombok.Getter;
import org.xml.sax.Attributes;

import java.util.Properties;
import java.util.function.BiConsumer;

@Getter
public enum PropertiesRootTag implements FlatTag< Properties >
{
    COMMENT( "comment", true, false ),
    ENTRY( "entry", true, false,

            // open: cache attributes.key
            ( properties, attributes ) -> properties
                    .setProperty(
                            "$currentKey",
                            attributes.getValue( "key" ) ),

            // close: de-cache attributes.key
            ( properties, text ) -> properties
                    .setProperty(
                            ( String ) properties.remove( "$currentKey" ),
                            text ) ),

    PROPERTIES( "*", false, true, ENTRY, COMMENT ),
    ROOT( "", false, false, PROPERTIES );


    private final String tag;
    private final FlatTag< Properties > self = this;
    private final boolean multiple;
    private final boolean choice;
    private final BiConsumer< Properties, Attributes > opener;
    private final BiConsumer< Properties, String > closer;
    private final Tag< ? super Properties, ? >[] children;

    @SafeVarargs
    PropertiesRootTag( String tag, boolean multiple, boolean choice, Tag< ? super Properties, ? >... children )
    {
        this( tag, multiple, choice, null, null, children );
    }

    @SafeVarargs
    PropertiesRootTag( String tag, boolean multiple, boolean choice, BiConsumer< Properties, Attributes > opener, BiConsumer< Properties, String > closer, Tag< ? super Properties, ? >... children )
    {
        this.tag = tag;
        this.multiple = multiple;
        this.choice = choice;
        this.opener = opener;
        this.closer = closer;
        this.children = children;
    }
}
